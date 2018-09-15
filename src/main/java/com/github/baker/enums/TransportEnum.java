package com.github.baker.enums;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: bakerZhu
 * @description: 数据传输方式
 * @time: 2018年09月15日
 * @modifytime:
 */
public enum  TransportEnum {
	HTTP("http"),
	KAFKA("kafka");

	private String type;

	TransportEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
