package com.mavis.mypanel.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mavis.mypanel.util.httputil.HttpResult;
import com.mavis.mypanel.util.httputil.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * docker 管理工具
 */
public class DockerRegistryUtil {

    private static final String API_MIRROR_LIST = "%s/v2/_catalog?n=1000";

    private static final String API_MIRROR_SHA = "%s/v2/%s/manifests/%s";

    private static final String API_MIRROR_TAGS = "%s/v2/%s/tags/list";

    private static final String API_MIRROR_DELETE = "%s/v2/%s/manifests/%s";

    private String BASE_MIRROR_URL;
    private String DOCKER_REGISTRY_USERNAME;
    private String DOCKER_REGISTRY_PASSWORD;

    private Map<String,String> getBaseAuthHeader(){
        HashMap<String, String> header = new HashMap<>();
        byte[] encode = Base64.getEncoder().encode(String.format("%s:%s", DOCKER_REGISTRY_USERNAME, DOCKER_REGISTRY_PASSWORD).getBytes(StandardCharsets.UTF_8));
        header.put("Authorization", "Basic " + new String(encode));
        return header;
    }

    /**
     * 获取仓库列表
     * @return
     */
    public List<String> getMirrorList(){
        String url = String.format(API_MIRROR_LIST, BASE_MIRROR_URL);
        HttpResult resp = HttpUtil.getMethod(url,getBaseAuthHeader(), "utf-8");
        if(resp != null){
            if("200".equals(resp.getStatuscodes())){
                String html = resp.getResponseHtml();
                JSONObject data = JSON.parseObject(html);
                JSONArray repositories = data.getJSONArray("repositories");
                List<String> list = repositories.toJavaList(String.class);
                return list;
            }
        }
        return null;
    }

    /**
     * 获取镜像sha值
     * @param mirrorname
     * @param version
     * @return
     */
    public JSONObject getMirrorInfo(String mirrorname,String version){
        if(StringUtils.isBlank(version)){
            //没有传版本则默认最新版
            version = "latest";
        }
        String url = String.format(API_MIRROR_SHA, BASE_MIRROR_URL, mirrorname,version);
        Map<String, String> headers = getBaseAuthHeader();
        headers.put("Accept","application/vnd.docker.distribution.manifest.v2+json");
        HttpResult resp = HttpUtil.getMethod(url, headers,"utf-8");
        if(resp != null){
            if("200".equals(resp.getStatuscodes())){
                String html = resp.getResponseHtml();
                JSONObject data = JSON.parseObject(html);
                if(data.containsKey("config")){
                    JSONObject config = data.getJSONObject("config");
                    JSONObject res = new JSONObject();
                    res.put("size",config.getInteger("size"));
                    res.put("digest",resp.getHeaderMap().get("Docker-Content-Digest"));
                    return res;
                }
            }
        }
        return null;
    }

    /**
     * 获取镜像tags标签列表
     * @param mirrorname
     * @return
     */
    private List<String> getMirrorTags(String mirrorname){
        String url = String.format(API_MIRROR_TAGS, BASE_MIRROR_URL, mirrorname);
        HttpResult resp = HttpUtil.getMethod(url,getBaseAuthHeader(), "utf-8");
        if(resp != null){
            if("200".equals(resp.getStatuscodes())){
                String html = resp.getResponseHtml();
                JSONObject data = JSON.parseObject(html);
                if(data.containsKey("tags")){
                    JSONArray tags = data.getJSONArray("tags");
                    if(tags != null){
                        return tags.toJavaList(String.class);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据sha删除仓库镜像
     * @param digest
     * @return
     */
    public boolean deleteMirrorTagsByDigest(String mirrorname, String digest){
        String url = String.format(API_MIRROR_DELETE, BASE_MIRROR_URL,mirrorname, digest);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader("Authorization", getBaseAuthHeader().get("Authorization"));
        CloseableHttpClient client = HttpUtil.createSSLClientDefault();
        HttpResponse resp = null;
        try {
            resp = HttpUtil.execute(client, httpDelete);
        } catch (Exception e) {
            return false;
        }
        if(resp != null && resp.getStatusLine() != null){
            if(resp.getStatusLine().getStatusCode() == 202){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取列表和版本信息
     * @return
     */
    public JSONObject getInfoList(){
        JSONObject res = new JSONObject();
        //获取所有镜像名称
        List<String> list = getMirrorList();
        if(list == null){
            return null;
        }
        CountDownLatch downLatch = new CountDownLatch(list.size());
        for (String mirrorname : list) {
            new Thread(() -> {
                List<String> tags = getMirrorTags(mirrorname);
                if(tags != null){
                    res.put(mirrorname,tags);
                }
                downLatch.countDown();
            }).start();
        }
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }

    //获取镜像的tags和详细信息
    public JSONArray getMirrorTagsInfo(String mirrorname){
        JSONArray arr = new JSONArray();
        List<String> mirrorTags = getMirrorTags(mirrorname);
        if(mirrorTags == null){
            return null;
        }
        List<String> tags = mirrorTags.stream().sorted().collect(Collectors.toList());
        if(tags != null){
            for (String tag : tags) {
                JSONObject info = getMirrorInfo(mirrorname, tag);
                if(info != null){
                    JSONObject obj = new JSONObject();
                    obj.put("mirrorname",mirrorname);
                    obj.put("tag",tag);
                    obj.put("size",info.getInteger("size"));
                    obj.put("digest",info.getString("digest"));
                    arr.add(obj);
                }
            }
        }
        return arr;
    }

    public DockerRegistryUtil(String BASE_MIRROR_URL, String DOCKER_REGISTRY_USERNAME, String DOCKER_REGISTRY_PASSWORD) {
        this.BASE_MIRROR_URL = BASE_MIRROR_URL;
        this.DOCKER_REGISTRY_USERNAME = DOCKER_REGISTRY_USERNAME;
        this.DOCKER_REGISTRY_PASSWORD = DOCKER_REGISTRY_PASSWORD;
    }

    public String getBASE_MIRROR_URL() {
        return BASE_MIRROR_URL;
    }

    public void setBASE_MIRROR_URL(String BASE_MIRROR_URL) {
        this.BASE_MIRROR_URL = BASE_MIRROR_URL;
    }

    public String getDOCKER_REGISTRY_USERNAME() {
        return DOCKER_REGISTRY_USERNAME;
    }

    public void setDOCKER_REGISTRY_USERNAME(String DOCKER_REGISTRY_USERNAME) {
        this.DOCKER_REGISTRY_USERNAME = DOCKER_REGISTRY_USERNAME;
    }

    public String getDOCKER_REGISTRY_PASSWORD() {
        return DOCKER_REGISTRY_PASSWORD;
    }

    public void setDOCKER_REGISTRY_PASSWORD(String DOCKER_REGISTRY_PASSWORD) {
        this.DOCKER_REGISTRY_PASSWORD = DOCKER_REGISTRY_PASSWORD;
    }

    public static void main(String[] args) {

    }
}
