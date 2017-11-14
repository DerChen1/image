package com.wykj.imgae.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgaeVo {
	private String imgurl;

	private Long projectid;

	private String createtime;

	private String flagId;
	private Integer height;
	private Integer width;
	private String title;

}
