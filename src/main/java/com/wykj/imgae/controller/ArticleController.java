package com.wykj.imgae.controller;




import com.google.common.collect.Maps;
import com.wykj.imgae.model.ImgaeVo;
import com.wykj.imgae.model.ResultVO;
import com.wykj.imgae.util.ResultVOUtil;
import com.wykj.imgae.util.TimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/resource")
@Api(value = "api接口")
public class ArticleController {

	/** 允许上传的扩展名 */
	private static final String[] extensionPermit = { "jpg", "png", "gif","jpeg" };

	Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * 文件上传
	 * @param response
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/imgUpload")
	@ResponseBody
	@ApiOperation(value="上传图片", notes="要求高 和 要求宽 可不传，" +
			"如果传了会判断图片的实际的宽高是否小于如果 ‘压缩比例’ 是否大于0（可选值 1.0 0.7 0.5 0.3） 会把图片压“等比”缩成要求宽高" +
			"（注意，等比压缩结果只能是高和宽其中之一等于目标宽高），" +
			"如果为0 则放弃压缩直接返回“上传图片失败，图片尺寸与要求不符”")
	public ResultVO imgUpload(@ApiParam(value="文件",required = true)@RequestParam(value = "file") MultipartFile file,
							  @RequestParam String merchantId,
							  @ApiParam(value = "要求高")@RequestParam(value = "height",required = false,defaultValue = "0")Integer height,
							  @ApiParam(value = "要求宽")@RequestParam(value = "width",required = false,defaultValue = "0")Integer width,
							  @ApiParam(value = "压缩比例")@RequestParam(value = "scale",required = false,defaultValue = "1.0f")Float scale,
							  HttpServletResponse response) {

		response.setContentType("image/jpeg");
		ByteArrayOutputStream os = null;
		ImgaeVo pimg = null;
		FileOutputStream out = null;
		String saveDirectoryPath = "/data/wwwroot/default/".concat(merchantId).concat("/");
			if (file.getOriginalFilename().length() > 0) {
				try {
					saveDirectoryPath = saveDirectoryPath + TimeUtil.getTimeYMD();
					String uuid = UUID.randomUUID().toString();
					StringBuilder name = new StringBuilder( "/" + uuid );
					File saveDirectory = new File(saveDirectoryPath);
					if (!saveDirectory.isDirectory() && !saveDirectory.exists()) {
						saveDirectory.mkdirs();
					}
					if (!file.isEmpty()) {
						String fileName = file.getOriginalFilename();
						String fileExtension = FilenameUtils.getExtension(fileName);
						BufferedImage image =null;
						name.append(".");
						name.append(fileExtension);

						if (!ArrayUtils.contains(extensionPermit, fileExtension)) {
							throw new Exception("No Support extension.");
						}else {
							image = ImageIO.read(file.getInputStream());
							saveDirectoryPath = saveDirectoryPath + name.toString();
							out = new FileOutputStream(saveDirectoryPath);
							if(height>0 && width>0 && scale >0){
								if (image.getHeight()>height || image.getWidth() >width){
									BufferedImage thumbnail  = Thumbnails.of(file.getInputStream()).size(width,height).keepAspectRatio(true).asBufferedImage();
									os = new ByteArrayOutputStream();
									ImageIO.write(thumbnail, fileExtension, os);
									out.write(os.toByteArray());
								}else{
									out.write(file.getBytes());
								}
							}else if (height>0 && width>0 && scale == 0 ){
								if (image.getHeight()>height || image.getWidth() >width){
									return ResultVOUtil.error(-1,"上传图片失败，图片尺寸与要求不符");
								}
							}else {
								if ("jpg".endsWith(fileExtension) || "png".endsWith(fileExtension) && file.getSize() > 1000000) {
									if(scale<=0.1f){
										scale = 1.0f;
									}
									BufferedImage thumbnail = Thumbnails.of(file.getInputStream()).scale(scale).outputQuality(0.5f)
											.asBufferedImage();
									os = new ByteArrayOutputStream();
									ImageIO.write(thumbnail, fileExtension, os);
									out.write(os.toByteArray());
								}else{
									out.write(file.getBytes());

								}
							}
							out.flush();
							out.close();
						}
						pimg = new ImgaeVo();
						pimg.setImgurl("http://resource.theyeasy.com/default/"+merchantId+"/" + TimeUtil.getTimeYMD() + name);
						pimg.setTitle(file.getOriginalFilename());
						pimg.setHeight(image.getHeight());
						pimg.setWidth(image.getWidth());
					}
				} catch (Exception e) {
					if (out!=null){
						try {
							out.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					if(os!=null){
						try {
							os.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					out = null;
					os = null;
					logger.info("上传异常", e);
					return ResultVOUtil.error(-1,"上传图片异常:"+e.getLocalizedMessage());
				}
			}
		return ResultVOUtil.success(pimg);
	}

	@ResponseBody
	@ApiOperation(value="上传文件", notes="文件名称" )
	@PostMapping("/fileUpload")
	public ResultVO fileUpload(@ApiParam(value="文件",required = true)@RequestParam(value = "file") MultipartFile file,
							   @RequestParam String merchantId){
		ByteArrayOutputStream os;
		ImgaeVo pimg = null;
		String saveDirectoryPath = "/data/wwwroot/default/".concat(merchantId).concat("/");
		StringBuilder name = null;
		if (file.getOriginalFilename().length() > 0){
			FileOutputStream out = null;
			try {
				saveDirectoryPath = saveDirectoryPath + TimeUtil.getTimeYMD();
				String uuid = UUID.randomUUID().toString();
				name = new StringBuilder( "/" + uuid );
				File saveDirectory = new File(saveDirectoryPath);
				if (!saveDirectory.isDirectory() && !saveDirectory.exists()) {
					saveDirectory.mkdirs();
				}
				if (!file.isEmpty()){
					String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
					name.append(".");
					name.append(fileExtension);
					out = new FileOutputStream(saveDirectoryPath+name.toString());
					out.write(file.getBytes());
					out.flush();
					out.close();
				}
			}catch (Exception e){
				if(out!=null){
					try {
						out.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				out = null;
			}
		}

		Map map = Maps.newHashMap();
		map.put("src","http://resource.theyeasy.com/default/"+merchantId+"/" + TimeUtil.getTimeYMD() + name.toString());
		return ResultVOUtil.success(map);
	}

}
