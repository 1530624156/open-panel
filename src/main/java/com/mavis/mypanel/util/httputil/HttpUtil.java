package com.mavis.mypanel.util.httputil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharArrayBuffer;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;


/**
 * 服务基类
 * 
 * @description
 * @author 舒超
 * @date 2017年12月11日
 */
public abstract class HttpUtil {

	private static Logger logger = Logger.getLogger(HttpUtil.class);
	
	/** 保持会话状态，每次请求保持同一个httpclient对象 */
	private static final boolean keepSameHttpClient = false;
	
    /** 连接超时时间 单位毫秒 */
	private static final int connectTimeout = 6000; // 改为6秒 20190103
    
    /** 请求获取数据的超时 单位毫秒 */
	private static final int socketTimeout = 6000; // 改为6秒 20190103
    
    /** 从connectManager获取Connection超时 单位毫秒 */
	private static final int connectionRequestTimeout = 5000; // 默认：5秒
    
    /** 最大连接数不要超过1000 */
	private static final int maxConnTotal = 100; // 默认：100个路由连接数量
    
    /** 实际的单个连接池大小 */
	private static final int maxConnPerRoute = 10; // 默认：单个路由可以创建10条线路
	
	// 定义web访问失败状态码
	public static final List<Integer> ERROR_CODE = Arrays.asList(400, 403, 404, 500, 501, 502, 503, 504);
	
	/** httpclient连接池 */
	private static CloseableHttpClient closeableHttpClient;
	
	// 记录excute耗时
	@SuppressWarnings("unused")
	private static long start = System.currentTimeMillis();
	
	static{
		if(keepSameHttpClient){
			// 初始化全局连接池
			closeableHttpClient = initHttpClient();
		}
	}
	
	 /**
	  * 记录url每分钟失败的次数
	  */
	protected static final LinkedHashMap<String, Integer> safe_count_cache = new LinkedHashMap<String, Integer>(1000, 0.75f, true) {
		private static final long serialVersionUID = 4893877948693835974L;
		@Override
		protected boolean removeEldestEntry(Entry<String, Integer> eldest) {
			return size() > 5000;
		}
	};
	/**
	 * 记录禁掉的url
	 */
	protected static final HashMap<String, Long> safe_forbid_cache = new HashMap<String, Long>();
	
	
    /**
     * 绕过验证-HTTPS
     *
     */
	private static SSLContext createIgnoreVerifySSL(){
//      System.setProperty("javax.net.debug","ssl");
    	SSLContext sslContext = null;
		try {
			System.setProperty("https.protocols", "TLSv1,TLSv1.2,SSLv2");
			System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
			System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
			sslContext = SSLContext.getInstance("TLS");
			X509TrustManager trustManager = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s)throws CertificateException {
					// TODO Auto-generated method stub
				}
				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s)throws CertificateException {
					// TODO Auto-generated method stub
				}
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { trustManager }, null);
		} catch (Exception e) {
			logger.error("异常 createIgnoreVerifySSL", e);
		}
		return sslContext;
	}
	
	/**
	 * 创建context上下文，设置cookie信息
	 * <br><br>
	 * 用于会话保持
	 * @param cookieStore
	 * @return
	 * @date 2017年12月18日
	 * @author 舒超
	 */
	public static HttpClientContext getContext(CookieStore cookieStore) {
		HttpClientContext context = HttpClientContext.create();
		Registry<CookieSpecProvider> registry = RegistryBuilder
				.<CookieSpecProvider> create()
				.register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
				.register(CookieSpecs.BROWSER_COMPATIBILITY,new BrowserCompatSpecFactory()).build();
		context.setCookieSpecRegistry(registry);
		context.setCookieStore(cookieStore);
		return context;
	}
	
	/**
	 * 设置全局的HttpClient连接池（前提：此工具类保持全局唯一httpclient）
	 * <br><br>
	 * 仅当keepSameHttpClient属性为true时此方法可用
	 * @return
	 * @date 2018年3月14日
	 * @author 舒超
	 */
	public static boolean setHttpClient(CloseableHttpClient httpclient){
		if(keepSameHttpClient){
			closeableHttpClient = httpclient;
			return true;
		}else{
			System.out.println("当前配置应用的httpclient连接池不是全局唯一的，如果需要主动设置连接池，请将keepSameHttpClient配置项设为false");
			return false;
		}
	}
	
	/**
	 * 创建并初始化context上下文
	 * <br><br>
	 * 用于会话保持
	 */
	public static HttpClientContext initContext() {
		return getContext(null);
	}
	
    /**
     * 初始化clinet请求对象-并初始化HTTP/HTTPS两种协议
     */
	public static CloseableHttpClient initHttpClient() {
		CloseableHttpClient httpclient = null;
        try {
//        	System.out.println("初始化连接池");
    		System.setProperty("jsse.enableSNIExtension", "false");
    		// 采用绕过验证的方式处理https请求
    		SSLContext sslcontext = createIgnoreVerifySSL();
    		// 设置协议http和https对应的处理socket链接工厂的对象
    		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
    				.register("http", PlainConnectionSocketFactory.INSTANCE)
    				.register("https", new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
    				.build();
    		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    		
    		RequestConfig defaultRequestConfig = RequestConfig.custom()
    				.setSocketTimeout(socketTimeout)
    				.setConnectTimeout(connectTimeout)
    				.setConnectionRequestTimeout(connectionRequestTimeout)
    				.setStaleConnectionCheckEnabled(true)
    				.setRedirectsEnabled(false) //不允许自动重定向
    				.setCircularRedirectsAllowed(false) //不允许循环重定向
    				.build();
			httpclient = HttpClients.custom()
    				.setDefaultRequestConfig(defaultRequestConfig)
    				.setMaxConnTotal(maxConnTotal)
    				.setMaxConnPerRoute(maxConnPerRoute)
    				.setConnectionManager(connManager)
    				.build();
			
        } catch (Exception e) {
           logger.error("异常 initHttpClient", e);
        }
        return httpclient;
    }
	
	public static CloseableHttpClient createSSLClientDefault() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				//信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
			RequestConfig defaultRequestConfig = RequestConfig.custom()
    				.setSocketTimeout(socketTimeout)
    				.setConnectTimeout(connectTimeout)
    				.setConnectionRequestTimeout(connectionRequestTimeout)
    				.setStaleConnectionCheckEnabled(true)
    				.setRedirectsEnabled(false) //不允许自动重定向
    				.setCircularRedirectsAllowed(false) //不允许循环重定向
    				.build();
			return HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).setMaxConnTotal(maxConnTotal)
    				.setMaxConnPerRoute(maxConnPerRoute).setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
		
		}
		return HttpClients.createDefault();

	}

	
	/**
     * 初始化clinet请求对象-并初始化HTTP/HTTPS两种协议(短超时)
     */
	public static CloseableHttpClient initHttpClient4shortTimeout() {
		CloseableHttpClient httpclient = null;
        try {
//        	System.out.println("初始化连接池");
    		System.setProperty("jsse.enableSNIExtension", "false");
    		// 采用绕过验证的方式处理https请求
    		SSLContext sslcontext = createIgnoreVerifySSL();
    		// 设置协议http和https对应的处理socket链接工厂的对象
    		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
    				.register("http", PlainConnectionSocketFactory.INSTANCE)
    				.register("https", new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
    				.build();
    		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    		
    		RequestConfig defaultRequestConfig = RequestConfig.custom()
    				.setSocketTimeout(8000)
    				.setConnectTimeout(8000)
    				.setConnectionRequestTimeout(5000)
    				.setStaleConnectionCheckEnabled(true)
    				.setRedirectsEnabled(false) //不允许自动重定向
    				.setCircularRedirectsAllowed(false) //不允许循环重定向
    				.build();
			httpclient = HttpClients.custom()
    				.setDefaultRequestConfig(defaultRequestConfig)
    				.setMaxConnTotal(100)
    				.setMaxConnPerRoute(10)
    				.setConnectionManager(connManager)
    				.build();
			
        } catch (Exception e) {
           logger.error("异常 initHttpClient", e);
        }
        return httpclient;
    }
	
	/**
	 * 创建get请求
	 * (模拟谷歌51浏览器头信息)
	 */
	public static HttpGet createHttpGet(String url){
		HttpGet method = new HttpGet(url);
		method.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.63 Safari/537.36"));
		method.addHeader(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		return method;
	} 
	/**
	 * 创建post请求
	 * (模拟谷歌51浏览器头信息)
	 */
	public static HttpPost createHttpPost(String url){
		HttpPost method = new HttpPost(url);
		method.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.63 Safari/537.36"));
		method.addHeader(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		return method;
	} 
	
	/**
	 * 设置头信息
	 * <br><br>
	 * 
	 * @param headerMap
	 * @param method
	 * @date 2018年1月24日
	 * @author 舒超
	 */
	public static void setHeader(Map<String,String> headerMap, HttpRequestBase method){
		if(headerMap != null && headerMap.size()>0){
        	Iterator<Entry<String,String>> it = headerMap.entrySet().iterator();
        	while(it.hasNext()){
        		Entry<String,String> entry = it.next();
        		if(StringUtil.isNotEmptyForAll(entry.getKey(), entry.getValue())){
        			if(method.getHeaders(entry.getKey())!=null){
        				method.removeHeaders(entry.getKey());
        			}
        			method.addHeader(entry.getKey(), entry.getValue());
        		}
        	}
    	}
	} 
	
    /**
     * 设置post请求参数
     * <br><br>
     * @param paramMap 普通key-value参数
     * @param paramCharset 参数编码
     * @param httpPost post请求
     * @date 2018年1月24日
     * @author 舒超
     */
    public static void setParam( Map<String,String> paramMap, String paramCharset, HttpPost httpPost) throws UnsupportedEncodingException {
    	setParam(paramMap, null, paramCharset, httpPost);
	}
	
    /**
     * 设置post请求参数
     * <br><br>
     * @param jsonParam json字符串消息参数
     * @param paramCharset 参数编码
     * @param httpPost post请求
     * @date 2018年1月24日
     * @author 舒超
     */
    public static void setParam(String jsonParam, String paramCharset, HttpPost httpPost) throws UnsupportedEncodingException {
    	setParam(null, jsonParam, paramCharset, httpPost);
	}
    
    /**
     * 设置post请求参数
     * <br><br>
     * 参数只能传一种，否则不会进行赋参
     * @param paramMap 普通key-value参数
     * @param jsonParam json字符串消息参数
     * @param paramCharset 参数编码
     * @param httpPost post请求
     * @date 2018年1月24日
     * @author 舒超
     */
    private static void setParam( Map<String,String> paramMap, String jsonParam, String paramCharset, HttpPost httpPost) throws UnsupportedEncodingException {
    	if(paramMap!=null && StringUtil.isEmpty(jsonParam)){
    		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
    		if(paramMap!=null && paramMap.size()>0){
    			Iterator<Entry<String,String>> it = paramMap.entrySet().iterator();
    			while(it.hasNext()){
    				Entry<String,String> entry = it.next();
    				String name = entry.getKey();
    				String value = entry.getValue();
    				formParams.add(new BasicNameValuePair(name, value));
    			}
    			if(StringUtil.isNotEmpty(paramCharset)){
    				httpPost.setEntity(new UrlEncodedFormEntity(formParams,paramCharset));
    			}else{
    				httpPost.setEntity(new UrlEncodedFormEntity(formParams));
    			}
    		}
    	}else if(jsonParam!=null && (paramMap==null||paramMap.isEmpty())){
    		StringEntity stringEntity = new StringEntity(jsonParam,paramCharset);
    		stringEntity.setContentEncoding(paramCharset);  
    		stringEntity.setContentType("application/json");//发送json数据需要设置contentType  
    		httpPost.setEntity(stringEntity);  
    	}
	}
    
	/**
	 * 发送请求
	 * <br><br>
	 * 
	 * @param httpClient
	 * @param method
	 * @param httpClientContext
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @date 2018年1月23日
	 * @author 舒超
	 */
	public static CloseableHttpResponse execute(CloseableHttpClient httpclient, HttpRequestBase method, HttpClientContext httpClientContext) throws Exception {
		CloseableHttpResponse httpResponse = null;
		// 如果连接池为空，初始化
		start = System.currentTimeMillis();
		if(httpClientContext!=null){
			httpResponse = httpclient.execute(method, httpClientContext);
		}else{
			httpResponse = httpclient.execute(method);
		}
		// 打印信息
		/*
		String methodString = method.getMethod();
		int statusCode =  httpResponse.getStatusLine().getStatusCode();
		String url = method.getURI().toString();
		String printMsg = "\n----【 执行{1}请求:(耗时{2}毫秒, 状态码：{3}) 】--{4}";
		logger.info(StringUtil.format(printMsg, methodString, System.currentTimeMillis()-start, statusCode, url));
		*/
		return httpResponse;
	}
	public static HttpResponse execute(CloseableHttpClient httpclient, HttpRequestBase method) throws Exception {
		return execute(httpclient, method, null);
	}
	
	/**
	 * 读取响应头
	 * <br><br>
	 * 
	 * @param allHeaders
	 * @return
	 * @date 2018年2月2日
	 * @author 舒超
	 */
    private static Map<String,String> getHeaderMap(Header[] allHeaders) {
    	Map<String,String> headerMap = new HashMap<String, String>();
		if(allHeaders==null || allHeaders.length<1) return headerMap;
		for(Header header:allHeaders){
			headerMap.put(header.getName(), header.getValue());
		}
		return headerMap;
	}
    

    /**
     * 保存响应结果
     * <br><br>
     * 
     * @param httpResult
     * @param closeableHttpResponse
     * @param httpClientContext
     * @param htmlCharset
     * @date 2018年2月5日
     * @author 舒超
     * @param allStatuscodes 
     */
	private static void saveResponseResult(HttpResult httpResult, String allStatuscodes, HttpResponse closeableHttpResponse,
                                           HttpClientContext httpClientContext, String htmlCharset) {
		String html = "";
		byte[] b = null;
		try {
			HttpEntity httpEntity = closeableHttpResponse.getEntity();
			if(httpEntity!=null){
				// 读取字节
				InputStream in = httpEntity.getContent();
				b = IOUtils.toByteArray(in);
				IOUtils.closeQuietly(in);
				Header header = httpEntity.getContentEncoding();
				// gzip解压处理
				if (header != null && "gzip".equals(header.getValue())) {
					// 如果内容编码是gzip格式，采用下面这种方式解析html
					b = unGZip(b);
				} 
				// 读取文本
				if(htmlCharset!=null && !htmlCharset.trim().equals("")){
					html = new String(b,htmlCharset);
				}else{
					html = new String(b);
				}
			}
		} catch (Exception e) {
			logger.error("saveResponseResult异常", e);
		}
		html = StringUtils.replace(html, "&nbsp;", " ").replace("&nbsp", " ");
		// 保存响应结果集
		httpResult.setStatuscodes(allStatuscodes);
		httpResult.setResponseByte(b);
		httpResult.setResponseHtml(html);
		httpResult.setCookieStore(httpClientContext.getCookieStore());
		httpResult.setHeaderMap(getHeaderMap(closeableHttpResponse.getAllHeaders()));
	}
	
    /**
     * 关闭连接
     * 
     * @param closeableHttpResponse
     * @param httpMethod
     * @date 2017年12月12日
     * @author 舒超
     */
    public static void closeClient (CloseableHttpClient httpclient, CloseableHttpResponse closeableHttpResponse, HttpGet httpGet, HttpPost httpPost) {
		//关闭底层连接
    	HttpClientUtils.closeQuietly(httpclient);
    	// 释放资源
		HttpClientUtils.closeQuietly(closeableHttpResponse);
		try {
			if (httpGet != null) {
				if (!httpGet.isAborted()) {
					httpGet.releaseConnection();
					httpGet.abort();
				}
				httpGet = null;
			}
			if (httpPost != null) {
				if (!httpPost.isAborted()) {
					httpPost.releaseConnection();
					httpPost.abort();
				}
				httpPost = null;
			}
		} catch (Exception e) {
//			logger.error("异常！ 关闭Http方法 ", e);
		}
    }
    
    /**
     * 服务器认证信息
     * <br><br>
     * 可用于basic认证
     * @param userName 登录信息的用户名
     * @param userPwd  登录信息的密码
     */
    public static void setCredentials(String userName, String userPwd, HttpClientContext httpClientContext){
        try {
            // 设置BasicAuth  
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            // Create the authentication scope  
            AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);
            // Create credential pair，在此处填写用户名和密码  
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, userPwd);
            // Inject the credentials  
            credsProvider.setCredentials(scope, credentials);  
            // 把AutoCache添加到上下文中
            httpClientContext.setCredentialsProvider(credsProvider);
        } catch (Exception e) {
           logger.error("创建服务器认证消息发生异常~setCredentials", e);
        }
    }
    
    /**
     * 发送get请求 (不带cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url){
    	return getMethod(url, "", null, "");
    }
    
    /**
     * 发送get请求 (不带cookie)
     * <br><br>
     * 
     * @param url
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, String htmlCharset){
    	return getMethod(url, "", null, htmlCharset);
    }
    
    /**
     * 发送get请求 (不带cookie)
     * <br><br>
     * 
     * @param url
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, String paramCharset, String htmlCharset){
    	return getMethod(url, paramCharset, null, htmlCharset);
    }
    
    /**
     * get请求
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, Map<String,String> headerMap){
    	return getMethod(url, "", headerMap, "");
    }
    
    /**
     * get请求
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
	 * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, Map<String,String> headerMap, String htmlCharset){
    	return getMethod(url, "", headerMap, htmlCharset);
    }
    
    /**
     * get请求
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, String paramCharset, Map<String,String> headerMap){
    	return getMethod(url, paramCharset, headerMap, "");
    }
    
    /**
     * get请求
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethod(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset){
    	return getMethod(url, paramCharset, headerMap, htmlCharset, null,null,null);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasic(String url, String basicUsername,String basicPassword){
    	return getMethodWithBasic(url, null,basicUsername,basicPassword);
    }
    
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasic(String url, String htmlCharset,String basicUsername,String basicPassword){
    	return getMethodWithBasic(url, null, null, htmlCharset ,basicUsername,basicPassword);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasic(String url, String paramCharset,String htmlCharset,String basicUsername,String basicPassword){
    	return getMethodWithBasic(url, paramCharset, null, htmlCharset, basicUsername,basicPassword);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasic(String url, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword){
    	return getMethodWithBasic(url, null, headerMap, htmlCharset, basicUsername,basicPassword);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasic(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword){
    	return getMethod(url, paramCharset, headerMap, htmlCharset, null,basicUsername,basicPassword);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie 会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasicAndCookie(String url,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return getMethodWithBasicAndCookie(url, null, null, null, basicUsername,basicPassword, cookieStore);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie 会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasicAndCookie(String url,String htmlCharset,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return getMethodWithBasicAndCookie(url, null, null, htmlCharset, basicUsername,basicPassword, cookieStore);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie 会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasicAndCookie(String url, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return getMethodWithBasicAndCookie(url, null, headerMap, htmlCharset, basicUsername,basicPassword, cookieStore);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie 会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasicAndCookie(String url, String paramCharset, String htmlCharset,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return getMethodWithBasicAndCookie(url, paramCharset, null, htmlCharset, basicUsername,basicPassword,cookieStore);
    }
    
    /**
     * get请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie 会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult getMethodWithBasicAndCookie(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return getMethod(url, paramCharset, headerMap, htmlCharset, cookieStore, basicUsername,basicPassword);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, CookieStore cookieStore){
    	return getMethodWithCookie(url, "", null, "", cookieStore);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, String htmlCharset, CookieStore cookieStore){
    	return getMethodWithCookie(url, "", null, htmlCharset, cookieStore);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (不填返回原始文本)
     * @return
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, String paramCharset, String htmlCharset, CookieStore cookieStore){
    	return getMethodWithCookie(url, paramCharset, null, htmlCharset, cookieStore);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, Map<String,String> headerMap, CookieStore cookieStore){
    	return getMethodWithCookie(url, "", headerMap, "", cookieStore);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	return getMethodWithCookie(url, "", headerMap, htmlCharset, cookieStore);
    }
    
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, String paramCharset, Map<String,String> headerMap, CookieStore cookieStore){
    	return getMethodWithCookie(url, paramCharset, headerMap, "", cookieStore);
    }
    /**
     * get请求 (保持会话，传入上一个请求的返回cookie)
     * <br><br>
     * 
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodWithCookie(String url, String paramCharset,  Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	return getMethod(url, paramCharset, headerMap, htmlCharset, cookieStore, null, null);
    }
    
    /**
     * get请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodAjax(String url, String paramCharset, String htmlCharset){
    	return getMethodAjax(url,paramCharset, null, htmlCharset);
    }
    
    /**
     * get请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodAjax(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset){
    	if(headerMap==null) headerMap = new HashMap<String, String>();
    	headerMap.put("X-Requested-With", "XMLHttpRequest");
    	return getMethod(url,paramCharset, headerMap, htmlCharset);
    }
    
    
    /**
     * get请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodAjaxWithCookie(String url, String paramCharset, String htmlCharset, CookieStore cookieStore){
    	return getMethodAjaxWithCookie(url,paramCharset, null, htmlCharset, cookieStore);
    }
    
    /**
     * get请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  getMethodAjaxWithCookie(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	if(headerMap==null) headerMap = new HashMap<String, String>();
    	headerMap.put("X-Requested-With", "XMLHttpRequest");
    	return getMethodWithCookie(url,paramCharset, headerMap, htmlCharset, cookieStore);
    }
    
    
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap){
    	return postMethod(url, paramMap, "", null, "" );
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap,Map<String,String> headerMap){
    	return postMethod(url, paramMap, "", headerMap, "");
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap,String htmlCharset){
    	return postMethod(url, paramMap, "", null, htmlCharset);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap){
     	return postMethod(url, paramMap, paramCharset, headerMap, "");
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap, String paramCharset, String htmlCharset){
    	return postMethod(url, paramMap, paramCharset, null, htmlCharset);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param headerMap 请求头
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap,Map<String,String> headerMap, String htmlCharset){
     	return postMethod(url, paramMap, "", headerMap, htmlCharset);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param headerMap 请求头
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethod(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset){
    	return postMethod(url, paramMap, null, paramCharset, headerMap, htmlCharset ,null, null,null);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, "", null, "" , cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap,Map<String,String> headerMap, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, "", headerMap, "", cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap,String htmlCharset, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, "", null, htmlCharset, cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param headerMap 请求头
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, paramCharset, headerMap, "", cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap, String paramCharset, String htmlCharset, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, paramCharset, null, htmlCharset, cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param headerMap 请求头
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap,Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	return postMethodWithCookie(url, paramMap, "", headerMap, htmlCharset, cookieStore);
    }
    
    /**
     * 发送post请求
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码(如果手动做了URI编码，此参数就为空)
     * @param headerMap 请求头
     * @param htmlCharset 抓取网页的编码(传空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodWithCookie(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	return postMethod(url, paramMap, null, paramCharset, headerMap, htmlCharset ,cookieStore,null,null);
    }
    
    /**
     * post请求 -- basic认证
     * <br><br>
     * 
     * @param url 请求地址
	 * @param paramMap 请求参数
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult postMethodWithBasic(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword){
    	return postMethod(url, paramMap, null, paramCharset, headerMap, htmlCharset, null, basicUsername,basicPassword);
    }
    
    /**
     * post请求 -- basic认证,携带cookies
     * <br><br>
     * 
     * @param url 请求地址
	 * @param paramMap 请求参数
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @param cookieStore cookie会话保持
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult postMethodWithBasicAndCookie(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset,String basicUsername,String basicPassword, CookieStore cookieStore){
    	return postMethod(url, paramMap, null, paramCharset, headerMap, htmlCharset, cookieStore, basicUsername,basicPassword);
    }
    
    /**
     * 发送post请求 -- 不带cookie
     * <br><br>
     * 可以以get请求的格式，此方法自动截取参数转换为post请求
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodOnGeturl (String url, String paramCharset, Map<String, String> headerMap, String htmlCharset) {
    	try {
    		String query = StringUtil.getQueryString(url);
    		url = StringUtil.getReqUrl(url);
    		// 参数存到map
    		Map<String,String> paramMap = new HashMap<String, String>();
    		if(!StringUtil.isEmpty(query)) {
    			String[] params = query.split("&");
    			for(String param:params) {
    				String key = StringUtils.substringBefore(param, "=");
    				String value = StringUtils.substringAfter(param, "=");
    				if(!StringUtil.isEmpty(key)) {
    					paramMap.put(key, value);
    				}
    			}
    		}
    		// 发送post请求
    		return postMethod(url, paramMap, paramCharset, headerMap, htmlCharset);
    	}catch (Exception e) {
    		logger.error("【postMethodOnGeturl error】",e);
    		return null;
		}
    }
    
    /**
     * 发送post请求 -- 保持会话，传入cookie
     * <br><br>
     * 可以以get请求的格式，此方法自动截取参数转换为post请求
     * @param url 请求地址
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    public static HttpResult  postMethodOnGeturlWithCookie (String url, String paramCharset, Map<String, String> headerMap, String htmlCharset, CookieStore cookieStore) {
    	try {
    		String query = StringUtil.getQueryString(url);
    		url = StringUtil.getReqUrl(url);
    		// 参数存到map
    		Map<String,String> paramMap = new HashMap<String, String>();
    		if(!StringUtil.isEmpty(query)) {
    			String[] params = query.split("&");
    			for(String param:params) {
    				String key = StringUtils.substringBefore(param, "=");
    				String value = StringUtils.substringAfter(param, "=");
    				if(!StringUtil.isEmpty(key)) {
    					paramMap.put(key, value);
    				}
    			}
    		}
    		// 发送post请求
    		return postMethodWithCookie(url, paramMap, paramCharset, headerMap, htmlCharset, cookieStore);
    	}catch (Exception e) {
    		logger.error("【postMethodOnGeturlWithCookie error】",e);
    		return null;
    	}
    }
    
    /**
     * 发送post请求--提交json格式参数
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param jsonParam 请求参数（json格式字符串）
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  postMethodUseJsonParam(String url, String jsonParam, String paramCharset, Map<String,String> headerMap, String htmlCharset){
    	if(headerMap==null) headerMap = new HashMap<String, String>();
    	if(StringUtil.isEmpty(paramCharset)) paramCharset= "UTF-8";
    	headerMap.put("Content-Type", "application/json;charset="+paramCharset);
    	return postMethod(url, null, jsonParam, paramCharset, headerMap, htmlCharset, null,null,null);
    }
    
    
    /**
     * 发送post请求--提交json格式参数
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param jsonParam 请求参数（json格式字符串）
     * @param paramCharset 参数URI编码 (如果参数已经做了URI编码，此参数就传空)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  postMethodUseJsonParamWithCookie(String url, String jsonParam, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
      	if(headerMap==null) headerMap = new HashMap<String, String>();
    	if(StringUtil.isEmpty(paramCharset)) paramCharset= "UTF-8";
    	headerMap.put("Content-Type", "application/json;charset="+paramCharset);
    	return postMethod(url, null, jsonParam, paramCharset, headerMap, htmlCharset, cookieStore,null,null);
    }
    
    /**
     * post请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码  (默认UTF-8)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  postMethodAjax(String url, Map<String,String> paramMap, String paramCharset, String htmlCharset){
    	return postMethodAjax(url, paramMap, paramCharset, null, htmlCharset);
    }
    
    /**
     * post请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (不带cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码  (默认UTF-8)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult  postMethodAjax(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset){
    	if(headerMap==null) headerMap = new HashMap<String, String>();
    	headerMap.put("X-Requested-With", "XMLHttpRequest");
    	return postMethod(url, paramMap, paramCharset, headerMap, htmlCharset);
    }
    
    
    /**
     * post请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码  (默认UTF-8)
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult postMethodAjaxWithCookie(String url, Map<String,String> paramMap, String paramCharset, String htmlCharset, CookieStore cookieStore){
    	return postMethodAjaxWithCookie(url, paramMap, paramCharset, null, htmlCharset, cookieStore);
    }
    
    /**
     * post请求-- ajax方式（通过XMLHttpRequest 异步提交）
     * <br><br>
     * (保持会话，传入上一个请求的返回cookie)
     * @param url 请求地址
     * @param paramMap 请求参数
     * @param paramCharset 参数编码  (默认UTF-8)
     * @param headerMap 请求头
     * @param htmlCharset 返回页面编码 (填空返回原始文本)
     * @return HttpResult
     * @date 2017年12月21日
     * @author 舒超
     */
    public static HttpResult postMethodAjaxWithCookie(String url, Map<String,String> paramMap, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore){
    	if(headerMap==null) headerMap = new HashMap<String, String>();
    	headerMap.put("X-Requested-With", "XMLHttpRequest");
    	return postMethodWithCookie(url, paramMap, paramCharset, headerMap, htmlCharset, cookieStore);
    }
    
    
    /**
     * 发送get请求
     * <br><br>
     * 
     * @param url 请求地址
     * @param headerMap 请求头信息
     * @param paramCharset 参数编码(不填默认系统编码)
     * @param htmlCharset 抓取网页的编码(不填返回原始文本)
     * @param cookieStore cookie信息(设置当前用户缓存的opac-cookie)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    private static HttpResult getMethod(String url, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore, String basicUsername, String basicPassword){
    	HttpResult httpResult = new HttpResult();
    	// 20190103
    	if(!checkSafe(url)) {
			logger.error("url访问失败过于频繁，暂时禁止访问，url=" + url);
			return httpResult;
		}
    	
    	if(StringUtil.isEmpty(url)) {
//    		logger.info("【错误】：请求地址为空！");
    		return null;
    	}
    	CloseableHttpClient httpclient = keepSameHttpClient ? closeableHttpClient : createSSLClientDefault();//initHttpClient()
    	// HttpClientContext上下文(会话保持)
    	HttpClientContext httpClientContext = getContext(cookieStore);
    	// basic认证
    	if(basicUsername!=null)
    		setCredentials(basicUsername, basicPassword, httpClientContext);
    	// 请求方法
    	HttpGet httpGet  = null;
    	CloseableHttpResponse closeableHttpResponse = null;
    	// 回参
    	
    	String allStatuscodes = "";
        try {
        	// 请求地址
        	url = StringUtil.isEmpty(paramCharset) ? url : StringUtil.uriEncode(url, paramCharset);
        	// 创建get请求
        	httpGet = createHttpGet(url);
        	// 设置自定义头信息
        	setHeader(headerMap, httpGet);
        	// 执行请求
			closeableHttpResponse = execute(httpclient, httpGet, httpClientContext);
			
			int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
			allStatuscodes += statusCode;
			// 重定向处理
			while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_SEE_OTHER || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
				closeClient(null, closeableHttpResponse, null, null);
				Header header = closeableHttpResponse.getFirstHeader("location");
				if (header == null) {
					break;
				}
				String newuri = header.getValue();
				//重定向 吉大图书馆 几个汉字乱码  特殊处理一下   20180614
				if(newuri.contains("202.198.25.15")){
					String head = StringUtils.substringBefore(newuri, "ps=");
					String after = StringUtils.substringAfter(newuri, "ps=");
					String[] buffer = after.split("/");
					url = head + "ps=" + buffer[0]+"/"+"吉大图书馆/"+buffer[2]+"/"+buffer[3]+"/"+buffer[4]+"/"+buffer[5];
				}else{
					url = StringUtil.concatUrlInCurPath(url, newuri);
				}
				// 保存重定向信息到结果集
				httpResult.setLocation(url);
				httpResult.redirectCountAdd1();
				// 释放原连接
				httpGet.releaseConnection();
				// 指向新地址
				httpGet.setURI(new URI(url));
				closeableHttpResponse = execute(httpclient, httpGet, httpClientContext);
				statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
				allStatuscodes += ">"+statusCode;
			}
			// 保存响应结果
			saveResponseResult(httpResult, allStatuscodes, closeableHttpResponse, httpClientContext, htmlCharset);
        }catch (Exception e) {
			logger.error("执行getMethod方法发生异常, 请求地址：["+url+"]", e);
			putSafeCache(url); // 20190103
		}finally{
			if(keepSameHttpClient){
				closeClient(null, closeableHttpResponse, httpGet, null);
			}else{
				closeClient(httpclient, closeableHttpResponse, httpGet, null);
			}
		}
        return httpResult;
    }
  

	/**
     * 发送post请求
     * <br><br>
     * 
     * @param url 请求地址 
     * @param paramMap 请求参数 
     * @param headerMap 请求头信息
     * @param paramCharset 参数进行编码
     * @param htmlCharset 抓取网页的编码(不填默认系统编码)
     * @param cookieStore cookie信息(设置当前用户缓存的opac-cookie)
     * @param basicUsername basic认证 账号
     * @param basicPassword basic认证 密码
     * @return HttpResult
     * @date 2017年12月12日
     * @author 舒超
     */
    private static HttpResult postMethod(String url, Map<String,String> paramMap, String jsonParam, String paramCharset, Map<String,String> headerMap, String htmlCharset, CookieStore cookieStore, String basicUsername, String basicPassword){
    	
    	// 20190103
    	if(!checkSafe(url)) {
			logger.error("url访问失败过于频繁，暂时禁止访问，url=" + url);
			return null;
		}
    	
    	if(StringUtil.isEmpty(url)) {
//    		logger.info("【错误】：请求地址为空！");
    		return null;
    	}
    	CloseableHttpClient httpclient = keepSameHttpClient ? closeableHttpClient : initHttpClient();
    	// HttpClientContext上下文(会话保持)
    	HttpClientContext httpClientContext = getContext(cookieStore);
    	// basic认证
    	if(basicUsername!=null)
    		setCredentials(basicUsername, basicPassword, httpClientContext);
    	// 请求方法
    	HttpPost httpPost = null;
    	HttpGet httpGet  = null;
    	CloseableHttpResponse closeableHttpResponse = null;
    	// 回参
    	HttpResult httpResult = new HttpResult();
    	String allStatuscodes = "";
        try {
        	// 请求地址
        	url = StringUtil.isEmpty(paramCharset) ? url : StringUtil.uriEncode(url, paramCharset);
        	// 创建请求
        	httpPost = createHttpPost(url);
        	// 设置头信息
        	setHeader(headerMap, httpPost);
        	// 设置参数(paramMap和jsonParam只能存在一个)
        	if(paramMap!=null && StringUtil.isEmpty(jsonParam)){
        		setParam(paramMap, paramCharset, httpPost); // 表单参数
        	}else if(jsonParam!=null && (paramMap==null||paramMap.isEmpty())){
        		setParam(jsonParam, paramCharset, httpPost);// json字符串格式参数
        	}
        	// 执行请求
			closeableHttpResponse = execute(httpclient, httpPost, httpClientContext);
			int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
			allStatuscodes += statusCode;
			// 重定向处理
			while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY || statusCode == HttpStatus.SC_MOVED_PERMANENTLY
					|| statusCode == HttpStatus.SC_SEE_OTHER || statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
				closeClient(null, closeableHttpResponse, null, null);
				// 获取请求转发地址
				Header header = closeableHttpResponse.getFirstHeader("location");
				if (header == null)  break;
				String newuri = header.getValue();
				url = StringUtil.concatUrlInCurPath(url, newuri);
				// 保存重定向信息到结果集
				httpResult.setLocation(url);
				httpResult.redirectCountAdd1();
				// 请求重定向
				if(httpGet==null || httpGet.isAborted()){
					httpGet = createHttpGet(url); // 无可用请求，创建新
					setHeader(headerMap, httpGet);// 设置头信息
				}else{
					httpGet.releaseConnection(); // 有可用请求，重置指向新地址 
					httpGet.setURI(new URI(url));
				}
				closeableHttpResponse = execute(httpclient, httpGet, httpClientContext);
				statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
				allStatuscodes += ">"+statusCode;
			}
			// 保存响应结果
			saveResponseResult(httpResult, allStatuscodes, closeableHttpResponse, httpClientContext, htmlCharset);
		} catch (Exception e) {
			logger.error("执行postMethod方法发生异常, 请求地址：["+url+"]", e);
			putSafeCache(url); // 20190103
		}finally{
			if(keepSameHttpClient){
				closeClient(null, closeableHttpResponse, httpGet, httpPost);
			}else{
				closeClient(httpclient, closeableHttpResponse, httpGet, httpPost);
			}
		}
        return httpResult;
    }
    
    
    /**
	 * / 如果内容是gzip压缩格式，采用下面这种方式读取文本
	 * @description: 
	 * @param entity
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @date 2017年12月12日
	 * @author 舒超
	 */
	public static String toStringForGzip(final HttpEntity entity) throws IOException, ParseException {
		return toStringForGzip(entity, null);
	}
    
    /**
	 * / 如果内容是gzip压缩格式，采用下面这种方式读取文本
	 * @description: 
	 * @param entity
	 * @param defaultCharset
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @date 2017年12月12日
	 * @author 舒超
	 */
	public static String toStringForGzip(final HttpEntity entity, final String defaultCharset) throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
		}
		int i = (int) entity.getContentLength();
		if (i < 0) {
			i = 4096;
		}
		Reader reader = null;
		if(defaultCharset!=null && !defaultCharset.trim().equals("")){
			reader = new InputStreamReader(new GZIPInputStream(instream, i), defaultCharset);
		}else{
			reader = new InputStreamReader(new GZIPInputStream(instream, i));
		}
			
		CharArrayBuffer buffer = new CharArrayBuffer(i);
		try {
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		}catch(Exception e){
//			logger.error(e);
		} finally {
			reader.close();
		}
		return buffer.toString();
	}
	
	/**
	 * 读取响应文本内容
	 * <br><br>
	 * 
	 * @param entity
	 * @param defaultCharset
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * @date 2018年2月5日
	 * @author 舒超
	 */
	public static String toString(final HttpEntity entity, final String defaultCharset) throws IllegalStateException, IOException{
		InputStream in = entity.getContent();
		byte[] b = IOUtils.toByteArray(in);
		IOUtils.closeQuietly(in);
		if(defaultCharset!=null && !defaultCharset.trim().equals("")){
			return new String(b,defaultCharset);
		}else{
			return new String(b);
		}
	}
	/**
	 * 读取响应文本内容
	 * <br><br>
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * @date 2018年2月5日
	 * @author 舒超
	 */
	public static String toString(final HttpEntity entity) throws IllegalStateException, IOException{
		return toString(entity, null);
	}
	/**
	 * 对字节数组进行gzip解压
	 * <br><br>
	 * 
	 * @param data
	 * @return
	 * @date 2018年2月5日
	 * @author 舒超
	 */
	public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception e) {
//        	logger.error(e);
        }
        return b;
    }
	
	/**
	 * 是否能访问指定url地址,8秒
	 * @param url
	 * @param maxNumber 重试次数
	 * @return
	 */
	public static boolean whetherAccess(String url, int maxNumber) {
		int num = 0; // 自动重试maxNumber次
		HttpResult hr = new HttpResult();
		try {
			while (num < maxNumber) {
				hr = getMethod(url);
				if(null==hr.getStatuscodes() || ERROR_CODE.contains(hr.getStatuscodes())){
					num++;
				} else {
					return true;
				}
			}
		} catch (Exception e) {
			logger.info(String.format("OPAC地址%s检测出现异常", url));
		}
		return false;
	}

	
	/**
	 * 获取熔断的key值
	 * 
	 * @author huanggaohua
	 * @date 2018年12月12日
	 * @param url
	 * @return
	 */
	protected static String getKey(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		if (url.contains("?")) {
			return url.substring(0, url.indexOf("?"));// 去掉参数
		} else {
			return url;
		}
	}
    
    
    
    /**
	 * @Title 访问失败的url放入缓存
	 * @Description
	 * @author lyz
	 * @date 2017年10月17日 下午9:17:57
	 * @param url
	 * @return void
	 */
	protected static void putSafeCache(String url) {
		if (StringUtils.isBlank(url)) {
			return;
		}
		String k1 = getKey(url);
		if (StringUtils.isBlank(k1)) {
			return;
		}
		String k2 = k1 + "_t_" + System.currentTimeMillis() / (60 * 1000);// 对1分钟求整作为key的后缀
		Integer c = safe_count_cache.get(k2);
		if (c == null) {
			c = 1;
		} else {
			c++;
		}
		if (c >= 15) {// 1分钟满15次，屏蔽
			Long t = safe_forbid_cache.get(k1);
			if (t == null) {
				safe_forbid_cache.put(k1, System.currentTimeMillis());
			}
		} else {
			safe_count_cache.put(k2, c);
		}
	}

	/**
	 * @Title 检测url是否已禁掉
	 * @Description
	 * @author lyz
	 * @date 2017年10月17日 下午9:18:27
	 * @param url
	 * @return boolean
	 */
	protected static boolean checkSafe(String url) {
		if (StringUtils.isBlank(url)) {
			return false;
		}
		String k = getKey(url);
		if (StringUtils.isBlank(k)) {
			return false;
		}
		Long t = safe_forbid_cache.get(k);
		if (t != null) {
			if ((System.currentTimeMillis() - t) < (5 * 60 * 1000)) {// 禁5分钟
				return false;
			} else {
				safe_forbid_cache.remove(k);
				return true;
			}
		}
		return true;
	}

	
	
	
	public static void main(String[] args) throws Exception {
		String url = "https://cas.nsjy.com.cn:8089/lyuapServer/serviceValidate?service=https%3A%2F%2Fnsjyzy.jw.chaoxing.com%2Fadmin%2Fcaslogin&ticket=ST-191315-96acfb61e9894d4ea413ed6d5d07a392";
		String html = cn.hutool.http.HttpUtil.get(url, 3000);
		System.out.println(html);
	}
}
