package com.mavis.mypanel.util.httputil;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static String iso2gb(String s) {
		if (s == null) {
			return "";
		}
		try {
			return new String(s.getBytes("ISO-8859-1"), "GBK").trim();
		} catch (Exception e) {
			return s;
		}
	}

	public static String gb2gb(String s) {
		if (s == null) {
			return "";
		}
		try {
			return new String(s.getBytes("GBK"), "GBK").trim();
		} catch (Exception e) {
			return s;
		}
	}

	public static String filteXMLString(String s) {
		if (s == null) {
			return "";
		}
		try {
			s = s.replaceAll("&", "&amp;");
			s = s.replaceAll("<", "&lt;");
			s = s.replaceAll(">", "&gt;");
			return s.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");

		} catch (Exception e) {
			return s;
		}
	}

	public static String Encode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, "GBK");
		} catch (Exception e) {
			return s;
		}
	}

	public static String Encode(String s, String ec) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, ec);
		} catch (Exception e) {
			return s;
		}
	}

	public static String Decode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLDecoder.decode(s, "GBK");
		} catch (Exception e) {
			return s;
		}
	}
	
	public static String utf8Decode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {
			return s;
		}
	}

	public static String cdata(String s) {
		if (s == null) {
			return "";
		}
		try {
			return "<![CDATA[" + s + "]]>";
		} catch (Exception e) {
			return s;
		}
	}

	public static String gb2iso(String s) {
		if (s == null) {
			return "";
		}
		try {
			return new String(s.getBytes("GBK"), "ISO-8859-1").trim();
		} catch (Exception e) {
			return s;
		}
	}

	public static String iso2utf8(String s) {
		if (s == null) {
			return "";
		}
		try {
			return new String(s.getBytes("ISO-8859-1"), "UTF-8").trim();
		} catch (Exception e) {
			return s;
		}
	}

	public static String utf82iso(String s) {
		if (s == null) {
			return "";
		}
		try {
			return new String(s.getBytes("UTF-8"), "ISO-8859-1").trim();
		} catch (Exception e) {
			return s;
		}
	}

	/**
	 * 给定字符串长度 大于根据指定长度 返回 start...end
	 * 
	 * @param _length
	 * @param _str
	 * @return
	 */
	public static String getStringPart(int _length, String _str) {
		if (_str == null) {
			return "";
		} else {
			if (_str.length() > _length) {
				return _str.substring(0, _length / 2) + "..." + _str.substring(_str.length() - _length / 2);
			} else {
				return _str;
			}
		}
	}

	public static String notNull(String s) {
		if (s == null) {
			return "";
		} else {
			return s.trim();
		}
	}

	public static String setHtmlTag(String input) {
		input = notNull(input);
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");
		input = input.replaceAll("\r", "");
		input = input.replaceAll("\n", "<br>");
		return input;
	}

	public static String getHtmlTag(String input) {
		input = notNull(input);
		input = input.replaceAll("<br>", "\r\n");
		input = input.replaceAll("&lt;", "<");
		input = input.replaceAll("&gt;", ">");
		return input;
	}

	/**
	 * Escape SQL tags, ' to ''; \ to \\.
	 * 
	 * @param input string to replace
	 * @return string
	 */
	public static String escapeSQLTags(String input) {
		if (input == null || input.length() == 0) {
			return input;
		}
		StringBuffer buf = new StringBuffer();
		char ch = ' ';
		for (int i = 0; i < input.length(); i++) {
			ch = input.charAt(i);
			if (ch == '\\') {
				buf.append("\\");
			} else if (ch == '\'') {
				buf.append("\'");
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	public static String escape(String src) {
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);
		for (i = 0; i < src.length(); i++) {
			j = src.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)) {
				tmp.append(j);
			} else if (j < 256) {
				tmp.append("%");
				if (j < 16) {
					tmp.append("0");
				}
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	public static String getPartString(String str, int len) throws UnsupportedEncodingException {
		byte b[];
		int counterOfDoubleByte = 0;
		b = str.getBytes("GBK");
		if (b.length <= len) {
			return str;
		}
		for (int i = 0; i < len; i++) {
			if (b[i] < 0) {
				counterOfDoubleByte++;
			}
		}
		if (counterOfDoubleByte % 2 == 0) {
			return new String(b, 0, len, "GBK") + "...";
		} else {
			return new String(b, 0, len - 1, "GBK") + "...";
		}
	}

	public static String htmlEncode(String txt) {
		txt = txt.replaceAll("&", "&amp;");
		txt = txt.replaceAll("&amp;amp;", "&amp;");
		txt = txt.replaceAll("&amp;quot;", "&quot;");
		txt = txt.replaceAll("\"", "&quot;");
		txt = txt.replaceAll("&amp;lt;", "&lt;");
		txt = txt.replaceAll("<", "&lt;");
		txt = txt.replaceAll("&amp;gt;", "&gt;");
		txt = txt.replaceAll(">", "&gt;");
		txt = txt.replaceAll("&amp;nbsp;", "&nbsp;");
		// txt = txt.replaceAll("'","&rsquo;");
		// txt = txt.replaceAll("&amp;rsquo;","&rsquo;");
		return txt;
	}

	public static String unHtmlEncode(String txt) {
		txt = txt.replaceAll("&amp;", "&");
		txt = txt.replaceAll("&quot;", "\"");
		txt = txt.replaceAll("&lt;", "<");
		txt = txt.replaceAll("&gt;", ">");
		txt = txt.replaceAll("&nbsp;", " ");
		return txt;
	}

	public static String markColor(String sw, String sTemp, String sColor) {
		String sReturn = "";
		int i = 0, j = 0;
		int iTempLength = sTemp.length();
		int iLengthS1 = sw.length();
		String sTemp1 = sw.toLowerCase();
		String sTemp2 = sTemp.toLowerCase();
		while (true) {
			i = sTemp2.indexOf(sTemp1, j);
			if (i == -1) {
				sReturn += sTemp.substring(j, iTempLength);
				break;
			}
			sReturn += sTemp.substring(j, i) + "<font color=\"" + sColor + "\">" + sTemp.substring(i, i + iLengthS1)
					+ "</font>";

			j = i + iLengthS1;
			if (j > iTempLength) {
				j = iTempLength;
			}
		}
		return sReturn;
	}

	public static Vector PraseSearchString(String sSearch) {
		if (sSearch == null) {
			return null;
		}

		String sSearchSTR = sSearch.trim();
		if (sSearchSTR.equals("")) {
			return null;
		}

		sSearchSTR = sSearchSTR.toLowerCase();
		Vector vValue = new Vector();
		int j = 0;
		while (true) {
			if (sSearchSTR.charAt(j) == ' ') {
				j++;
			} else {
				int i = j;
				j = sSearchSTR.indexOf(" ", i);
				if (j == -1) {
					vValue.add(sSearchSTR.substring(i, sSearchSTR.length()));// sSearch +=
					// sSearchSTR.substring(i,sSearchSTR.length());
					break;
				} else {
					vValue.add(sSearchSTR.substring(i, j));// sSearch += sSearchSTR.substring(i,j) + " ";
				}
			}
		}
		return vValue;
	}

	public static String MarkColor_V1(String sKeyWord, String sDate, String sColor) {
		if ((sKeyWord == null) || sKeyWord.equals("")) {
			return sDate;
			// String sFont=sColor;//"<font color=\""+sColor+"\">";
		}

		Vector vValue = PraseSearchString(sKeyWord);
		String sTemp = sDate;
		try {
			sTemp = sTemp.replaceAll("\\&\\$\\@", "");// font
			sTemp = sTemp.replaceAll("\\$\\@\\&", "");// color
			sTemp = sTemp.replaceAll("\\@\\$\\&", "");// sColor
			sTemp = sTemp.replaceAll("\\#\\$\\@", "");// <a href=
			sTemp = sTemp.replaceAll("\\#\\@\\$", "");// target="_blank" >
			sTemp = sTemp.replaceAll("\\#\\@\\*", "");// </a>
			sTemp = sTemp.replaceAll("\\#\\*\\@", "");// <br>
			// sTemp=sTemp.replaceAll("\\^\\$\\&","");//原文快照
			// sTemp=sTemp.replaceAll("\\^\\$\\&","");//获取原文
			sTemp = sTemp.replaceAll("&nbsp;", "");// &nbsp;
			sTemp = sTemp.replaceAll("<a href=", "\\#\\$\\@");
			sTemp = sTemp.replaceAll("target=\"_blank\">", "\\#\\@\\$");
			sTemp = sTemp.replaceAll("</a>", "\\#\\@\\*");
			sTemp = sTemp.replaceAll("<br>", "\\#\\*\\@");
			// sTemp=sTemp.replaceAll(new String("原文快照".getBytes("ISO-8859-1")),"\\!\\$\\&");
			// sTemp=sTemp.replaceAll(new String("原文快照".getBytes("ISO-8859-1")),"\\!\\$\\&");
			int[] lenArr = new int[vValue.size()];
			int[] sortArr = new int[vValue.size()];
			int j = 0, k = 0;
			for (j = 0; j < vValue.size(); j++) {
				lenArr[j] = vValue.get(j).toString().length();
				sortArr[j] = j;
			}
			int temp = 0, temp1 = 0;
			int len = lenArr.length;
			for (k = len; k > 0; k--) {
				for (j = 0; j < k - 1; j++) {
					if (lenArr[j] < lenArr[j + 1]) {
						temp = lenArr[j];
						lenArr[j] = lenArr[j + 1];
						lenArr[j + 1] = temp;

						temp1 = sortArr[j];
						sortArr[j] = sortArr[j + 1];
						sortArr[j + 1] = temp1;
					}
				}
			}
			for (int i = 0; i < len; i++) {
				String sKey = vValue.get(sortArr[i]).toString();
				sTemp = replaceMarkColor(sKey, sTemp);
			}
			sTemp = sTemp.replaceAll("\\#\\$\\@", "<a href=");
			sTemp = sTemp.replaceAll("\\#\\@\\$", "target=\"_blank\">");
			sTemp = sTemp.replaceAll("\\#\\@\\*", "</a>");
			sTemp = sTemp.replaceAll("\\#\\*\\@", "<br>");
			sTemp = sTemp.replaceAll("\\&\\$\\@", "font");
			sTemp = sTemp.replaceAll("\\$\\@\\&", "color");
			sTemp = sTemp.replaceAll("\\@\\$\\&", sColor);
			// sTemp=sTemp.replaceAll("\\!\\$\\&",new String("原文快照".getBytes("ISO-8859-1")));
			// sTemp=sTemp.replaceAll("\\!\\$\\&",new String("原文快照".getBytes("ISO-8859-1")));
			// sTemp=sTemp.replaceAll(StringUtil.iso2gb("原文快照"),StringUtil.iso2gb("<font
			// color=\"#7777CC\">原文快照</font>"));
			// sTemp=sTemp.replaceAll(StringUtil.iso2gb("获取原文"),StringUtil.iso2gb("<font
			// color=\"#7777CC\">获取原文</font>"));
		} catch (Exception ex) {
			System.out.print(ex.toString());
		}
		return sTemp;
	}

	public static String replaceMarkColor(String sKey, String sDate) {
		String sReturn = "";
		int i1 = 0, i2 = 0, i3 = 0;
		while (true) {
			i1 = sDate.indexOf("#$@", i2);
			if (i1 != -1) {
				String stemp = sDate.substring(i2, i1);
				sReturn += replaceAllFont(stemp, sKey);
				i2 = sDate.indexOf("#@$", i1) + 3;
				sReturn += sDate.substring(i1, i2);// 加上连接
			} else {
				if (i2 < sDate.length()) {
					sReturn += replaceAllFont(sDate.substring(i2, sDate.length()), sKey);
				}
				break;
			}
		}// while
		return sReturn;
	}

	public static String replaceAllFont(String sTemp, String s1) {
		int i = 0, j = 0;
		String sReturn = "";
		int iTempLength = sTemp.length();
		int iLengthS1 = s1.length();
		String sTemp2 = sTemp.toLowerCase();
		while (true) {
			i = sTemp2.indexOf(s1, j);
			if (i == -1) {
				sReturn += sTemp.substring(j, iTempLength);
				break;
			}
			sReturn += sTemp.substring(j, i) + "<&$@ $@&=@$& name=\"duxiu\">" + sTemp.substring(i, i + iLengthS1)
					+ "</&$@>";

			j = i + iLengthS1;
			if (j > iTempLength) {
				j = iTempLength;
			}
		}
		return sReturn;
	}

	/**
	 * 将一个 string 格式化为整数
	 * 
	 * @author 贺博
	 * @param s
	 * @param default_value 转化失败 返回默认值
	 * @return 格式后的整数
	 */
	public static int formatIntger(String s, int default_value) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return default_value;
		}
	}

	public static int formatIntger(String s) {
		return formatIntger(s, 0);
	}

	/**
	 * 获得字符串 数组中的值
	 * 
	 * @author 贺博
	 * @param arr
	 * @param index
	 * @param default_value
	 * @return
	 */
	public static String arrIndex2String(String[] arr, int index, String default_value) {
		try {
			return arr[index];
		} catch (Exception e) {
			return default_value;
		}
	}

	public static String arrIndex2String(String[] arr, int index) {
		return arrIndex2String(arr, index, "");
	}

	/**
	 * 判断一个字符串是否为空
	 * 
	 * @author 贺博
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return (s == null || s.trim().equals("")) ? true : false;
	}
	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}
	public static boolean isEmpt(String s) {
		return (s == null || s.equals("")) ? true : false;
	}

	/**
	 * ' to \' ; " to \"
	 * 
	 * @author 贺博
	 * @param s
	 * @return
	 */
	public static String transferQuote(String s) {
		return isEmpt(s) ? "" : s.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"");
	}

	/**
	 * SQL Server检索中,将带有空格的字符替换成%来检索.
	 * 
	 * @param sw
	 * @return
	 */
	public static String praseSearchSQLBlank(String sw) {
		// 替换半角空格
		sw = sw.replaceAll(" +", "%");
		// 替换全角空格
		sw = sw.replaceAll("　+", "%");
		return sw;
	}

	public static String praseSSNumberToImgDicEnc(String sSSNumber) {
		String sImg = "";
		if (!sSSNumber.equals("") && !sSSNumber.equals("none") && !sSSNumber.equals("NULL")) {
			try {
				sImg += sSSNumber.substring(0, 2) + "/";
				sImg += sSSNumber.substring(2, 5) + "/";
				sImg += sSSNumber.substring(5, sSSNumber.length());
			} catch (Exception e)// try
			{
				sImg = "";
			}
		}
		return sImg;
	}

	public static String[] split(String sourse, String spliter) {
		sourse = notNull(sourse);
		int index = 0, nextIndex = 0, spliterLen = spliter.length();
		ArrayList<String> list = new ArrayList<String>();
		while ((nextIndex = sourse.indexOf(spliter, index)) != -1) {
			list.add(sourse.substring(index, nextIndex));
			index = nextIndex + spliterLen;
		}
		if (list.size() > 0) {
			list.add(sourse.substring(sourse.lastIndexOf(spliter) + spliter.length(), sourse.length()));
		}
		String splits[] = new String[list.size()];
		list.toArray(splits);

		return splits;
	}

	/**
	 * String join
	 * 
	 * @author 贺博
	 * @date 2008-4-9
	 * @param s
	 * @param delimiter 连接符
	 * @return
	 */
	public static String join(Collection s, String delimiter) {
		StringBuilder sb = new StringBuilder();
		Iterator iter = s.iterator();
		if (iter.hasNext()) {
			sb.append(iter.next());
			while (iter.hasNext()) {
				sb.append(delimiter);
				sb.append(iter.next());
			}
		}
		return sb.toString();
	}

	/**
	 * 使用反射，效率比较差
	 * 
	 * @author <a href="mailto:hb562100@163.com">贺博</a>
	 * @date 2008-5-28
	 * @param array
	 * @param delimiter
	 * @return
	 */
	public static String join(Object array, String delimiter) {
		if (array.getClass().isArray()) {
			StringBuilder sb = new StringBuilder();
			int len = Array.getLength(array);

			if (len > 0) {
				sb.append(Array.get(array, 0));
			}

			for (int i = 1; i < len; i++) {
				sb.append(delimiter);
				sb.append(Array.get(array, i));
			}

			return sb.toString();
		} else {
			throw new IllegalArgumentException(array + " is not a array.");
		}
	}

	public static String join(int[] arr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		int len = arr.length;

		if (len > 0) {
			sb.append(arr[0]);
		}

		for (int i = 1; i < len; i++) {
			sb.append(delimiter);
			sb.append(arr[i]);
		}

		return sb.toString();
	}

	/**
	 * String join
	 * 
	 * @author 贺博
	 * @date 2008-4-10
	 * @param delimiter 连接符
	 * @return
	 */
	public static String join(String[] arr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		int len = arr.length;

		if (len > 0) {
			sb.append(arr[0]);
		}

		for (int i = 1; i < len; i++) {
			sb.append(delimiter);
			sb.append(arr[i]);
		}

		return sb.toString();
	}

	/**
	 * 在源字符串中查找分割符之间的字符串,查找到返回源字符串所在的位置,没有找到返回-1。如在"1503,1502"中查找"0"则返回-1,查找1502则返回6
	 * 
	 * @param source 源字符串
	 * @param find 要查找的字符串
	 * @param delimiter 分割符
	 * @return
	 */
	public static int indexOf(String source, String find, String delimiter) {
		if (isEmpt(source) || isEmpt(find)) {
			return -1;
		}

		if (!source.startsWith(delimiter)) {
			source = delimiter + source;
		}
		if (!source.endsWith(delimiter)) {
			source = source + delimiter;
		}
		find = delimiter + find + delimiter;
		return source.indexOf(find);
	}

	/**
	 * 验证email格式是否正确
	 * @return
	 */
	public static boolean checkEmail(String line) {
		Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		Matcher m = p.matcher(line);
		return m.find();
	}

	/**
	 * 验证用户的手机号是否正确
	 * 
	 * @param phoneNum
	 * @return
	 */
	public static boolean checkPhoneNum(String phoneNum) {
		return phoneNum.matches("^\\d{11}$");
	}

	/**
	 * 组合原文，MD5加密
	 * 
	 * @param source
	 * @return
	 */
	public static String getMD5(Object... source) {
		if (source == null) {
			return null;
		}
		String str = "";
		for (Object o : source) {
			str = str.concat("[" + o.toString() + "]");
		}
		try {
			return Crypt.md5(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean startsWithAny(String string, String... searchStrings) {
		if (isEmpty(string) || ArrayUtils.isEmpty(searchStrings)) {
			return false;
		}
		for (int i = 0; i < searchStrings.length; i++) {
			String searchString = searchStrings[i];
			if (StringUtils.startsWith(string, searchString)) {
				return true;
			}
		}
		return false;
	}

	public static boolean startsWith(String str, String prefix) {
		return startsWith(str, prefix, false);
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		return startsWith(str, prefix, true);
	}

	private static boolean startsWith(String str, String prefix, boolean ignoreCase) {
		if (str == null || prefix == null) {
			return (str == null && prefix == null);
		}
		if (prefix.length() > str.length()) {
			return false;
		}
		return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
	}

	public static String gbk2gb2312(String s) {
		if (s == null)
			return "";
		try {
			return new String(s.getBytes("GBK"), "GB2312").trim();
		} catch (Exception e) {
			return s;
		}
	}

	// 自定义编码
	public static String customCode(String s, String oldCode, String newCode) {
		if (s == null)
			return "";
		try {
			return new String(s.getBytes(oldCode), newCode).trim();
		} catch (Exception e) {
			return s;
		}
	}
	
	/**
	 * 判断一组字符串是都不为空
	 *  
	 * @param s
	 * @return
	 */
	public static boolean isNotEmptyForAll(String... s) {
		return !isEmpty(s);
	}
	
	/**
	 * 判断一组字符串是否为空, 只要有一个为空就返回true
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String... s) {
		for (String str : s) {
			if (isEmpty(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将str转成url地址
	 * 
	 * @param str
	 * @return
	 */
	public static String utf8Url(String str) {
		if (isEmpty(str)) {
			return "";
		}
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 将str转成gb2312地址
	 * 
	 * @param str
	 * @return
	 */
	public static String gbUrl(String str) {
		try {
			return URLEncoder.encode(str, "GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 格式化字符串 <code><pre>
	 * format("ab{1}d{2}_{1}" , "c","e") // out put : abcde_c
	 * </pre></code> 使用{ldelim}, {rdelim} 表示"{","}"
	 * 
	 * @param str 使用{1},{2},...{n}占位符的字符串
	 * @param args 填充参数数组
	 * @return
	 */
	public static String format(String str, Object... args) {
		if (isEmpty(str)) {
			return "";
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i] != null)
				str = str.replace("{" + (i + 1) + "}", args[i].toString());
		}
		return str.replaceAll("\\{\\d\\}", "").replaceAll("\\{ldelim\\}", "{").replaceAll("\\{rdelim\\}", "}");
	}

	private static final Pattern FORMAT_STRING = Pattern.compile("\\{([^\\s]+?)}");

	/**
	 * <code><pre>
	 * format("{name}:{sex}", Map {
	 * 		name:"Tom",
	 * 		sex:"boy"}) // out put : Tom:boy
	 * </pre></code> 使用{ldelim}, {rdelim} 表示"{","}"
	 * 
	 * @param str 使用{key}占位符的字符串
	 * @param args 填充Map
	 * @return
	 */
	public static String format(String str, Map<String, ?> args) {
		if (isEmpty(str)) {
			return "";
		}
		str = str.replaceAll("\\{ldelim\\}", "{").replaceAll("\\{rdelim\\}", "}");
		Matcher matcher = FORMAT_STRING.matcher(str);
		while (matcher.find()) {
			String k = matcher.group(1);
			Object val = args.get(k);
			if (val != null) {
				String s = val.toString();
				str = str.replaceAll("\\{" + k + "\\}", s);
			} else {
				str = str.replaceAll("\\{" + k + "\\}", "");
			}
		}

		return str;
	}

	/**
	 * 赋值初始值
	 * 
	 * @param s
	 * @param defaultv
	 * @return
	 */
	public static String forDefault(String s, String defaultv) {
		return isEmpty(s) ? defaultv : s;
	}

	/**
	 * Match regex , not global match ,return the first match.
	 * 
	 * @author <a href="mailto:hb562100@163.com">HeBo</a>
	 * @param s input string
	 * @param regex regex
	 * @return No match return null,or index [0] store full input , index[1]...index[n] store group 1-n;
	 */
	public static String[] match(CharSequence s, String regex) {
		Pattern p = Pattern.compile(regex);
		return match(s, p);
	}

	/**
	 * Match regex , not global match ,return the first match.
	 * 
	 * @author <a href="mailto:hb562100@163.com">HeBo</a>
	 * @param s input string
	 * @param regex regex
	 * @return No match return null,or index [0] store full input , index[1]...index[n] store group 1-n;
	 */
	public static String[] match(CharSequence s, Pattern regex) {
		Matcher m = regex.matcher(s);

		String[] l = null;

		if (m.find()) {
			l = new String[m.groupCount() + 1];

			for (int i = 0; i < l.length; i++)
				l[i] = m.group(i);
		}

		return l;
	}

	/**
	 * bytes to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytes2Hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (byte b : bytes) {
			int i = b & 0xFF;
			if (i <= 0xF) {
				sb.append("0");
			}

			sb.append(Integer.toHexString(i));
		}

		return sb.toString();
	}

	public static final List<Character> HEX_CHAR_LIST;
	static {
		HEX_CHAR_LIST = new ArrayList<Character>();
		HEX_CHAR_LIST.add(new Character('0'));
		HEX_CHAR_LIST.add(new Character('1'));
		HEX_CHAR_LIST.add(new Character('2'));
		HEX_CHAR_LIST.add(new Character('3'));
		HEX_CHAR_LIST.add(new Character('4'));
		HEX_CHAR_LIST.add(new Character('5'));
		HEX_CHAR_LIST.add(new Character('6'));
		HEX_CHAR_LIST.add(new Character('7'));
		HEX_CHAR_LIST.add(new Character('8'));
		HEX_CHAR_LIST.add(new Character('9'));
		HEX_CHAR_LIST.add(new Character('a'));
		HEX_CHAR_LIST.add(new Character('b'));
		HEX_CHAR_LIST.add(new Character('c'));
		HEX_CHAR_LIST.add(new Character('d'));
		HEX_CHAR_LIST.add(new Character('e'));
		HEX_CHAR_LIST.add(new Character('f'));
	}

	private static byte hex2Byte(String s) {
		int high = HEX_CHAR_LIST.indexOf(new Character(s.charAt(0))) << 4;
		int low = HEX_CHAR_LIST.indexOf(new Character(s.charAt(1)));

		return (byte) (high + low);
	}

	/**
	 * hex string to bytes
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] hex2Bytes(String input) {
		int len = input.length() / 2;
		byte[] rtn = new byte[len];

		for (int i = 0; i < len; i++) {
			rtn[i] = hex2Byte(input.substring(i * 2, i * 2 + 2));
		}
		return rtn;
	}

	/**
	 * Parses the string argument as a signed decimal integer.Default 0;
	 * 
	 * @param o
	 * @return
	 */
	public static int parseInt(Object o) {
		return parseInt(o, 0);
	}

	/**
	 * 转成Integer ,失败返回空
	 * <br><br>
	 * 
	 * @param o
	 * @return 
	 * @date 2018年4月12日
	 * @author 舒超
	 */
	public static Integer parseInteger(Object o) {
		if (o == null)
			return null;
		try {
			return Integer.parseInt(o.toString().trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
	/**
	 * Parses the string argument as a signed decimal integer.
	 * 
	 * @param o
	 * @param defaultV
	 * @return
	 */
	public static int parseInt(Object o, int defaultV) {
		if (o == null)
			return defaultV;
		try {
			return Integer.parseInt(o.toString().trim());
		} catch (NumberFormatException e) {
			return defaultV;
		}
	}

	/**
	 * Decorate a string, return <i>decorator + s + decorator</i>
	 * 
	 * @param s
	 * @param decorator
	 * @return
	 */
	public static String wrap(Object s, String decorator) {
		return decorator + s + decorator;
	}

	/**
	 * Return a integer
	 * 
	 * @param number
	 * @param defaultV
	 * @return
	 */
	public static int intValue(Number number, int defaultV) {
		if (number == null) {
			return defaultV;
		}

		return number.intValue();
	}

	/**
	 * Return a integer , default 0
	 * 
	 * @param number
	 * @param defaultV
	 * @return
	 */
	public static int intValue(Number number) {
		return intValue(number, 0);
	}

	/**
	 * Return a long
	 * 
	 * @param number
	 * @param defaultV
	 * @return
	 */
	public static long longValue(Number number, long defaultV) {
		if (number == null) {
			return defaultV;
		}

		return number.longValue();
	}

	/**
	 * Return a long , default 0
	 * 
	 * @param number
	 * @param defaultV
	 * @return
	 */
	public static long longValue(Number number) {
		return intValue(number, 0);
	}

	/**
	 * Specailed for length ,return start...end. <code><pre>
	 * www.duxiu.com/readPage.jsp?dxid=123456789
	 * if length = 20 get 'www.duxiu.com/r...56789'
	 * </pre></code>
	 * 
	 * @param length
	 * @param str
	 * @return
	 */
	public static String pad(int _length, String _str) {
		if (_str == null)
			return "";
		else {
			if (_str.length() > _length) {
				return _str.substring(0, _length * 3 / 4) + "..." + _str.substring(_str.length() - _length / 4);
			} else {
				return _str;
			}
		}
	}

	/**
	 * Specailed for length ,return start...end. <code><pre>
	 * www.duxiu.com/readPage.jsp?dxid=123456789
	 * if length = 20 get 'www.duxiu.com/r...56789'
	 * </pre></code>
	 *
	 * @return
	 */
	public static String pad(int _length, CharSequence _str) {
		if (_str == null)
			return "";
		else {
			if (_str.length() > _length) {
				return _str.subSequence(0, _length * 3 / 4) + "..."
						+ _str.subSequence(_str.length() - _length / 4, _str.length());
			} else {
				return _str.toString();
			}
		}
	}

	public static String trim(String str) {
		if(str==null) {
			return null;
		}else{
			return str.trim();
		}
	}
	
	/**
	 * 获取项目类文件根路径
	 * <br><br>
	 * 
	 * @return
	 * @date 2018年1月9日
	 * @author 舒超
	 */
	public static String getClassPath(){
		String path = "";
		try {
			path = StringUtil.class.getClassLoader().getResource("").getPath();
		} catch (NullPointerException e) {
			path = StringUtil.class.getClassLoader().getResource("allOpac.properties").getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}
	
	/**
	 * 格式化拼接url 
	 * <BR/><BR/>
	 * 应用场景（当你抓取curUrl页面的时候，curUrl页面中有个a标签的跳转地址是redirectUrl，而redirectUrl可能只是一个相对地址，这时 可以使用这个方法来生成一个完成的跳转地址）
	 * @param curUrl 当前地址(必须是合法的url)
	 * @param redirectUrl 下一个要跳转的页面,(可以不是完整的地址，相对于curUrl路径的地址)
	 * @return 拼接好的url
	 * @date 2017年12月11日
	 */
	public static String concatUrlInCurPath(String curUrl, String redirectUrl){
		try {
			if(redirectUrl==null) return "";
			if(redirectUrl.startsWith("http://") || redirectUrl.startsWith("https://")){
				return redirectUrl;
			}
			URL url = new URL(curUrl);
			
			String domain = "";
			String Protocol = "http";
			Protocol = url.getProtocol();
			domain = url.getHost();
			int port = url.getPort();
			if(port!=-1) {
				domain += ":"+port;
			}
			String baseUrl = Protocol + "://" + domain;
			
			String path = url.getPath();
			
			while(path!=null && path.contains("//")){
				path = path.replace("//", "/");
			}
			path = path.substring(0,path.lastIndexOf("/") + 1);
			while(redirectUrl!=null && redirectUrl.contains("//")){
				redirectUrl = redirectUrl.replace("//", "/");
			}
			if(redirectUrl.startsWith("/")){
				return baseUrl + redirectUrl;
			}else if(isEmpty(path)){
				return baseUrl + "/" + redirectUrl;
			}else {
				if(!path.startsWith("/")){
					path = "/" + path;
				}
				if(!path.endsWith("/")){
					path = path + "/";
				}
			}
			return  baseUrl + path + redirectUrl;
		} catch (Exception e) {
			System.out.println(curUrl+"不是合法的地址");
			return redirectUrl;
		}
	}
	
	/**
	 * 获取服务器访问根路径
	 * 
	 * @description: 
	 * @param url
	 * @return
	 * @date 2017年12月12日
	 * @author 舒超
	 * @throws MalformedURLException 
	 */
	public static String getWebBaseUrl(String url) throws MalformedURLException {
		URL u = new URL(url);
		String Protocol = u.getProtocol();
		String domain = u.getHost();
		int port = u.getPort();
		if(port!=-1) {
			domain += ":"+port;
		}
		return Protocol + "://" + domain;
	}
	
	/**
	 * 获取请求地址
	 * <br><br>
	 * 除去参数部分
	 * @param url
	 * @return
	 * @date 2017年12月21日
	 * @author 舒超
	 */
	public static String getReqUrl(String url){
		try {
			if(url.indexOf("?")!=-1){
				url = url.substring(0,url.indexOf("?"));
			}
			return url;	
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * 根据url获取参数部分
	 * 
	 * @description: 
	 * @param url 地址链接
	 * @return 参数-标准格式(例a=1&b=2&c=3)
	 * @date 2017年12月12日
	 * @author 舒超
	 */
	public static String getQueryString(String url){
		return getQueryString(url, null);
	}
	
	/**
	 * 根据url获取参数部分
	 * 
	 * @description: 
	 * @param url 地址链接
	 * @param charset 对参数进行编码
	 * @return 参数-标准格式(例a=1&b=2&c=3)
	 * @date 2017年12月12日
	 * @author 舒超
	 */
	public static String getQueryString(String url, String charset){
		String query = "";
		if(url==null||url.trim().equals("")) return query;
		if(url.indexOf("?")!=-1){
			query = url.substring(url.indexOf("?") + 1);
		}
		if(isNotEmpty(charset)){
			String ss[] = query.split("&");
			StringBuffer sb = new StringBuffer();
			for(String s: ss){
				if(s!=null && s.indexOf("=")!=-1){
					if(s.split("=").length>=1){
						sb.append(Encode(s.split("=")[0],charset));
					}
					sb.append("=");
					if(s.split("=").length>=2){
						sb.append(Encode(s.substring(s.indexOf("=")+1), charset));
					}
				}else{
					sb.append(s);
				}
				sb.append("&");
			}
			query = sb.toString().substring(0,sb.length()-1);
		}
		query = clearQuery(query);
		return query;
	}
	
	/**
	 * 格式化参数，去除首尾多余的"&"
	 * 
	 * @param query 请求地址的参数部分
	 * @return 
	 * @date 2017年12月12日
	 * @author 舒超
	 */
	public static String clearQuery(String query){
		if(isEmpty(query)) return "";
		while(query.startsWith("&")){
			query = query.substring(1);
		}
		while(query.endsWith("&")){
			query = query.substring(0,(query.length()-1));
		}
		return query;
	}
	
	/**
	 * 对get请求进行url编码
	 * <br><br>
	 * 只会对参数进行编码，不会对请求地址及协议编码
	 * @param url
	 * @param charset(不填默认UTF-8)
	 * @return
	 * @date 2018年1月15日
	 * @author 舒超
	 */
	public static String uriEncode(String url, String charset) {
		String requrl = getReqUrl(url);
		if(isEmpty(charset)){
			charset = "UTF-8";
		}
		String query = getQueryString(url, charset);
		if(isEmpty(query)){
			return requrl;
		}else{
			return requrl + "?" + query;
		}
	}
	
	public static void main(String[] args) {
	 
	}
}
