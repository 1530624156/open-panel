package com.mavis.mypanel.util.httputil;

import org.apache.http.client.CookieStore;

import java.util.Map;

/**
 * http|https 网络资源获取工具包
 * <br><br>
 * 用于保存爬虫常用的响应结果
 * @Date 2018年2月5日
 * @Author 舒超
 */
public class HttpResult {
	
	// 请求状态码，如果发生重定向，返回状态码通过">"隔开 ，例：302>302>200
	private String statuscodes;
	// 请求响应结果(文本形式保存)
	private String responseHtml;
	// 请求响应结果(字节形式保存)
	private byte[] responseByte;
	// 响应cookie信息
	private CookieStore cookieStore;
	// 响应头集合
	private Map<String,String> headerMap;
	// 重定向次数
	private short redirectCount = 0;
	// 最终重定向地址
	private String location;
	
	/** 获取响应内容(文本形式，例如html) */
	public String getResponseHtml() {
		if(responseHtml==null && responseByte!=null){
			return new String(responseByte);
		}
		return responseHtml;
	}
	/** 设置响应内容(文本形式，例如html) */
	public void setResponseHtml(String responseHtml) {
		this.responseHtml = responseHtml;
	}
	/** 获取响应内容(字节形式, 例如图片、PDF等非文本文件, 以字节保存) */
	public byte[] getResponseByte() {
		if(responseByte==null && responseHtml!=null){
			return responseHtml.getBytes();
		}
		return responseByte;
	}
	/** 设置响应内容(字节形式, 例如图片、PDF等非文本文件, 以字节保存) */
	public void setResponseByte(byte[] responseByte) {
		this.responseByte = responseByte;
	}
	/** 获取响应cookie */
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	/** 设置响应cookie */
	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}
	/** 获取响应头集合 */
	public Map<String, String> getHeaderMap() {
		return headerMap;
	}
	/** 设置响应头集合 */
	public void setHeaderMap(Map<String, String> headerMap) {
		this.headerMap = headerMap;
	}
	/** 获取请求重定向次数 */
	public short getRedirectCount() {
		return redirectCount;
	}
	/** 设置重定向次数 */
	public void redirectCountAdd1() {
		this.redirectCount = (short) (this.redirectCount + 1);
	}
	/** 获取重定向地址 */
	public String getLocation() {
		return location;
	}
	/** 设置重定向地址 */
	public void setLocation(String location) {
		this.location = location;
	}
	/** 获取请求状态码，例如：200。 如果发生重定向，返回状态码通过">"隔开 ，例：302>302>200 */
	public String getStatuscodes() {
		return statuscodes;
	}
	public void setStatuscodes(String statuscodes) {
		this.statuscodes = statuscodes;
	}
	
}
