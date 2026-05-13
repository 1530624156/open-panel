package com.mavis.mypanel.entity.vo;

import com.alibaba.fastjson.JSON;

/**
 * JsonReturn
 * @author yangyg
 * @version 1.0
 *
 */
public class JsonReturn {
    private Integer result;
    private String msg;
    private Object data;
    public Integer getResult() {
        return result;
    }
    public void setResult(Integer result) {
        this.result = result;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @return ：@return {@link String }
	 * @throws
	 * @Title：toJsonString
	 * @Description：转换字符串
	 * @author ：zjy
	 * @date ：2022-10-24 13:55:32
	 */
	public String toJsonString() {
		return JSON.toJSONString(this);
	}
	
	public static JsonReturn success(Object data) {
		return success(null, data);
	}

	public static JsonReturn successMsg(String msg) {
		return success(msg, null);
	}

	public static JsonReturn success(String msg, Object data) {
		return success(1, msg, data);
	}

	public static JsonReturn success(Integer code, String msg, Object data) {
		JsonReturn jr = new JsonReturn();
		jr.setResult(code != null ? code : 1);
		jr.setMsg(msg);
		jr.setData(data);
		return jr;
	}

	public static JsonReturn error(Object data) {
		return error(null, data);
	}

	public static JsonReturn errorMsg(String msg) {
		return error(msg, null);
	}

	public static JsonReturn error(String msg, Object data) {
		return error(0, msg, data);
	}

	public static JsonReturn error(Integer code, String msg, Object data) {
		JsonReturn jr = new JsonReturn();
		jr.setResult(code != null ? code : 0);
		jr.setMsg(msg);
		jr.setData(data);
		return jr;
	}
	//
	//public static JsonReturn errorI18(String msg, Object data) {
	//	if(StringUtils.isNotBlank(msg)){
	//		msg = I18nLocaleHolder.getMessage(msg);
	//	}
	//	return error(0, msg, data);
	//}
	//
	//public static JsonReturn successI18(String msg, Object data) {
	//	if(StringUtils.isNotBlank(msg)){
	//		msg = I18nLocaleHolder.getMessage(msg);
	//	}
	//	return success(1,msg,data);
	//}

	@Override
	public String toString() {
		return "JsonReturn{" +
				"result=" + result +
				", msg='" + msg + '\'' +
				", data=" + data +
				'}';
	}

	public boolean isSuccess() {
		return result != null && result == 1;
	}
}
