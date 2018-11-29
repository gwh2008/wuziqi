package com.weguard.websocket;

/**
 * 
 * @author gaowh
 *
 */
public class Result {
	/**
	 * 落子坐标
	 */
	private String xy;
	/**
	 * 发送消息
	 */
	private String message;
	/**
	 * 是否允许落子
	 */
	private boolean bout;
	/**
	 * 落子颜色
	 */
	private String color;
	public String getXy() {
		return xy;
	}
	public void setXy(String xy) {
		this.xy = xy;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isBout() {
		return bout;
	}
	public void setBout(boolean bout) {
		this.bout = bout;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	
	
}
