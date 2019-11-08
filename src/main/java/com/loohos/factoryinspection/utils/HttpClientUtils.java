package com.loohos.factoryinspection.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpClientUtils {
    private static Logger logger = Logger.getLogger(HttpClientUtils.class);
    //最大线程池
    public static final int THREAD_POOL_SIZE = 5;

    public interface HttpClientDownloadProgress{
        public void onProgress(int progress);
    }

    private static HttpClientUtils httpClientDownload;
    private ExecutorService downloadExecutorService;

    private HttpClientUtils(){
        downloadExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public static HttpClientUtils getInstance(){
        if(httpClientDownload == null){
            httpClientDownload = new HttpClientUtils();
        }
        return httpClientDownload;
    }

    //Download files
    public void download(final String url, final String filePath){
        downloadExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                httpDownloadFile(url, filePath, null, null);
            }
        });
    }

    public void download(final String url, final String filePath, final HttpClientDownloadProgress progress){
        downloadExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                httpDownloadFile(url, filePath, progress, null);
            }
        });
    }

    private void httpDownloadFile(String url, String filePath, HttpClientDownloadProgress progress, Map<String, String> headMap){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet(url);
            setGetHead(httpGet, headMap);
            CloseableHttpResponse response1 = httpClient.execute(httpGet);
            try{
                logger.info(response1.getStatusLine());
                HttpEntity httpEntity = response1.getEntity();
                long contentLength = httpEntity.getContentLength();
                InputStream is = httpEntity.getContent();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int r = 0;
                long totalRead = 0;
                while ((r = is.read(buffer)) > 0){
                    os.write(buffer, 0, r);
                    totalRead += r;
                    if(progress != null){
                        progress.onProgress((int)(totalRead * 100 / contentLength));
                    }
                }
                FileOutputStream fos = new FileOutputStream(filePath);
                os.writeTo(fos);
                os.flush();
                os.close();
                fos.close();
                EntityUtils.consume(httpEntity);
            }finally {
                response1.close();;
            }

        }catch (Exception e){
            e.printStackTrace();

        }finally {
            try {
                httpClient.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }

        }
    }

    //get
    public String httpGet(String url){
        return httpGet(url, null);
    }

    public String httpGet(String url, Map<String, String> headMap){
        String responseContent = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response1 = httpClient.execute(httpGet);
            setGetHead(httpGet, headMap);
            try{
                logger.info(response1.getStatusLine());
                HttpEntity entity = response1.getEntity();
                responseContent = getRespString(entity);
                logger.debug(responseContent);
                EntityUtils.consume(entity);
            }finally {
                response1.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                httpClient.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
        return responseContent;
    }

    public String httpPost(String url, Map<String, String> paramsMap){
        return httpPost(url, paramsMap, null);
    }

    public String httpPost(String url, Map<String, String> paramsMap, Map<String, String> headMap){
        String responseContent = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            HttpPost httpPost = new HttpPost(url);
            setPostHead(httpPost, headMap);
            setPostParams(httpPost, paramsMap);
            CloseableHttpResponse response1 = httpClient.execute(httpPost);
            try {
                System.out.println(response1.getStatusLine());
                HttpEntity entity = response1.getEntity();
                responseContent = getRespString(entity);
                EntityUtils.consume(entity);
            }finally {
                response1.close();
            }

        }catch (Exception e){

        }finally {
            try {
                httpClient.close();
            }catch (IOException e2){
                e2.printStackTrace();
            }
        }
        System.out.println("responseContent = " + responseContent);
        return responseContent;
    }

    //设置post参数
    private void setPostParams(HttpPost httpPost, Map<String, String> paramsMap) throws Exception{
        if(paramsMap != null && paramsMap.size() > 0){
            List<NameValuePair> nvps = new ArrayList<>();
            Set<String> keySet = paramsMap.keySet();
            for(String key : keySet){
                nvps.add(new BasicNameValuePair(key, paramsMap.get(key)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        }
    }

    private void setPostHead(HttpPost httpPost, Map<String, String> headMap){
        if(headMap != null && headMap.size() > 0){
            Set<String> keySet = headMap.keySet();
            for(String key : keySet){
                httpPost.addHeader(key, headMap.get(key));
            }
        }
    }

    private void setGetHead(HttpGet httpGet, Map<String, String> headMap){
        if(headMap != null && headMap.size() > 0){
            Set<String> keySet = headMap.keySet();
            for(String key : keySet){
                httpGet.addHeader(key, headMap.get(key));
            }
        }
    }

    //upload file
    public String uploadFileImpl(String serverUrl, String localFilePath,
                                 String serverFieldName, Map<String, String> params)
            throws Exception{
        String respStr = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try{
            HttpPost httpPost = new HttpPost(serverUrl);
            FileBody binFileBody = new FileBody(new File(localFilePath));

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addTextBody("fileId", "aaaa", ContentType.APPLICATION_JSON);
            //add the file params
            multipartEntityBuilder.addPart(serverFieldName, binFileBody);


            //setUploadParams(multipartEntityBuilder, params);

            HttpEntity reqEntity = multipartEntityBuilder.build();
            httpPost.setEntity(reqEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            try{
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                respStr = getRespString(resEntity);
                EntityUtils.consume(resEntity);
            }finally {
                response.close();
            }
        }finally {
            httpClient.close();
        }
        System.out.println("respStr = " + respStr);
        return respStr;
    }

    private void setUploadParams(MultipartEntityBuilder multipartEntityBuilder, Map<String, String> params){
        if(params != null && params.size() > 0){
            Set<String> keySet = params.keySet();
            for(String key : keySet){
                multipartEntityBuilder
                        .addPart(key, new StringBody(params.get(key), ContentType.TEXT_PLAIN));
            }
        }
    }

    private String getRespString(HttpEntity entity) throws Exception{
        if(entity == null) return  null;
        InputStream is = entity.getContent();
        StringBuffer strBuf = new StringBuffer();
        byte[] buffer = new byte[4096];
        int r = 0;
        while ((r = is.read(buffer)) > 0){
            strBuf.append(new String(buffer, 0, r, "UTF-8"));
        }
        return strBuf.toString();
    }


    public String doPostWithJson(String url, Map<String, String> headMap, JSONObject jsonObject){
        String responseContent = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            //Set timeout
            // 构造消息头
            setPostHead(httpPost, headMap);

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonObject.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            try {
                logger.info(""+httpResponse.getStatusLine());
                HttpEntity responseEntity = httpResponse.getEntity();
                responseContent = getRespString(responseEntity);
                EntityUtils.consume(entity);
            }finally {
                httpResponse.close();
            }

            // 检验返回码
            //int statusCode = httpResponse.getStatusLine().getStatusCode();
            //statusCode != HttpStatus.SC_OK

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                httpClient.close();
            }catch (IOException e2){
                e2.printStackTrace();
            }
        }
        logger.info("HttpClientPost, response content: " + responseContent);
        return responseContent;
    }

    public String doPostWithJsonArray(String url, Map<String, String> headMap, JSONArray jsonArray){
        String responseContent = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            //Set timeout
            // 构造消息头
            setPostHead(httpPost, headMap);

            // 构建消息实体
            StringEntity entity = new StringEntity(jsonArray.toString(), Charset.forName("UTF-8"));
            entity.setContentEncoding("UTF-8");
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            logger.info("array start to transfer"+ httpPost);
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            try {
                logger.info(">>>>>>>>>>>"+httpResponse.getStatusLine());
                HttpEntity responseEntity = httpResponse.getEntity();
                responseContent = getRespString(responseEntity);
                EntityUtils.consume(entity);
            }finally {
                httpResponse.close();
            }

            // 检验返回码
            //int statusCode = httpResponse.getStatusLine().getStatusCode();
            //statusCode != HttpStatus.SC_OK

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                httpClient.close();
            }catch (IOException e2){
                e2.printStackTrace();
            }
        }
        logger.info("HttpClientPost, response content: " + responseContent);
        return responseContent;
    }

    // 构建唯一会话Id
    public static String getSessionId(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
    }

    public static String getUUID(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString().trim();
        str = str.replaceAll("-", "");
        return str;
    }

    public static String getGatewayJsonByUrl(String gatewayUrl, String gatewayToken) {
        String retInfo = "";
        HttpPost post = null;
        try {
            post = new HttpPost(gatewayUrl);
            HttpClient httpClient = new DefaultHttpClient();
            //Set timeout
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
            // 构造消息头
            post.setHeader("Content-Type","application/x-www-form-urlencoded"); //url编码格式消息头
            post.setHeader("Connection", "Close");
            String sessionId =  getSessionId();
            post.setHeader("SessionId", sessionId);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(!gatewayToken.equals("")){
                params.add(new BasicNameValuePair("token", gatewayToken));
                post.setEntity(new UrlEncodedFormEntity(params));
            }

            HttpResponse httpResponse = httpClient.execute(post);
            // 检验返回码
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                logger.error("请求出错: "+ statusCode);
            }else{
                int retCode = 0;
                String sessendId = "";
                // 返回码中包含retCode及会话Id
                for(Header header : httpResponse.getAllHeaders()){
                    if(header.getName().equals("retcode")){
                        retCode = Integer.parseInt(header.getValue());
                    }
                    if(header.getName().equals("SessionId")){
                        sessendId = header.getValue();
                    }
                }
                HttpEntity responseEntity = httpResponse.getEntity();
                retInfo  = EntityUtils.toString(responseEntity, "UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(post != null){
                try{
                    post.releaseConnection();
                    Thread.sleep(500);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        return  retInfo;
    }
}
