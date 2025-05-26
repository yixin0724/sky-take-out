package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     * @param url 请求的URL地址
     * @param paramMap 请求参数的键值对，如果不需要参数，则可以传入null
     * @return 返回请求的结果，以字符串形式表示
     */
    public static String doGet(String url,Map<String,String> paramMap){
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 用于存储请求结果的变量
        String result = "";
        // 用于存储HTTP响应的对象
        CloseableHttpResponse response = null;

        try{
            // 创建URIBuilder对象，用于构建请求的URI
            URIBuilder builder = new URIBuilder(url);
            // 如果存在参数，将参数添加到URI中
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            // 构建URI
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if(response.getStatusLine().getStatusCode() == 200){
                // 如果响应状态为200，将响应内容转换为字符串并存储到result变量中
                result = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            // 关闭response和httpClient对象
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 返回请求结果
        return result;
    }

    /**
     * 发送POST方式请求
     * @param url 请求的URL
     * @param paramMap 请求参数的键值对
     * @return 响应的内容
     * @throws IOException 当请求过程中发生I/O错误时抛出此异常
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            // 设置请求配置
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            // 获取并返回响应内容
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                // 关闭响应对象以释放资源
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     * @param url 请求的URL
     * @param paramMap 请求参数的键值对
     * @return 响应结果的字符串表示
     * @throws IOException 当请求过程中发生I/O错误时抛出
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 如果有参数，则处理参数并设置到请求中
            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            // 设置请求配置
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            // 获取并返回响应结果
            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            // 如果发生异常，重新抛出以便调用者处理
            throw e;
        } finally {
            // 确保在finally块中关闭response
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 返回响应结果字符串
        return resultString;
    }
    /**
     * 构建请求配置
     * 该方法用于构建一个自定义的请求配置对象，该对象用于配置HTTP请求的各种参数
     * 主要配置了三种超时时间，以确保请求在预期时间内响应，从而提高系统的稳定性和用户体验
     * @return RequestConfig 返回一个自定义配置的RequestConfig对象
     */
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC) // 设置连接超时时间
                .setConnectionRequestTimeout(TIMEOUT_MSEC) // 设置请求连接的超时时间
                .setSocketTimeout(TIMEOUT_MSEC).build(); // 设置读取数据的超时时间
    }

}
