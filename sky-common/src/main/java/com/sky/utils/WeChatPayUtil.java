package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 微信支付工具类
 */
@Component
public class WeChatPayUtil {

    //微信支付下单接口地址
    public static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    //申请退款接口地址
    public static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 获取调用微信接口的客户端工具对象
     * @return
     */
    private CloseableHttpClient getClient() {
        PrivateKey merchantPrivateKey = null;
        try {
            //merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题
            merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath())));
            //加载平台证书文件
            X509Certificate x509Certificate = PemUtil.loadCertificate(new FileInputStream(new File(weChatProperties.getWeChatPayCertFilePath())));
            //wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉
            List<X509Certificate> wechatPayCertificates = Arrays.asList(x509Certificate);

            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatProperties.getMchid(), weChatProperties.getMchSerialNo(), merchantPrivateKey)
                    .withWechatPay(wechatPayCertificates);

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            CloseableHttpClient httpClient = builder.build();
            return httpClient;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送post方式请求
     *
     * @param url 请求的URL地址
     * @param body 请求的主体内容
     * @return 返回响应的主体内容
     * @throws Exception 当请求过程中发生错误时抛出异常
     */
    private String post(String url, String body) throws Exception {
        // 获取HttpClient实例
        CloseableHttpClient httpClient = getClient();

        // 创建HttpPost对象，用于发送POST请求
        HttpPost httpPost = new HttpPost(url);
        // 设置请求头，指定接受的响应类型为JSON
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        // 设置请求头，指定发送的内容类型为JSON
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        // 设置请求头，添加微信支付的商户序列号
        httpPost.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());
        // 设置请求的主体内容
        httpPost.setEntity(new StringEntity(body, "UTF-8"));

        // 执行HTTP请求并获取响应
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            // 将响应内容转换为字符串并返回
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            // 关闭httpClient和response以释放资源
            httpClient.close();
            response.close();
        }
    }

    /**
     * 发送get方式请求
     * @param url 请求的URL地址
     * @return 返回响应的字符串内容
     * @throws Exception 抛出异常，包括但不限于网络异常、解析异常等
     */
    private String get(String url) throws Exception {
        // 创建HttpClient客户端实例
        CloseableHttpClient httpClient = getClient();

        // 创建HttpGet请求实例，传入URL地址
        HttpGet httpGet = new HttpGet(url);
        // 设置请求头，接受的响应类型为JSON
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        // 设置请求头，发送的内容类型为JSON
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        // 设置请求头，微信支付序列号
        httpGet.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());

        // 执行HttpGet请求，获取响应
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            // 将响应内容转换为字符串并返回
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            // 关闭httpClient和response资源，释放系统资源
            httpClient.close();
            response.close();
        }
    }


    /**
     * 发起微信支付JSAPI请求
     *
     * @param orderNum 订单号，用于标识每一笔交易
     * @param total 订单总金额，以元为单位
     * @param description 订单描述，用于在支付通知中展示
     * @param openid 用户在微信的唯一标识符，用于指定支付用户
     * @return 返回微信支付的响应内容，包括支付状态、交易号等信息
     * @throws Exception 如果网络请求失败或解析响应出错，抛出异常
     */
    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        // 创建JSON对象，用于封装微信支付请求的参数
        JSONObject jsonObject = new JSONObject();
        // 设置微信支付的APPID
        jsonObject.put("appid", weChatProperties.getAppid());
        // 设置微信支付的商户号
        jsonObject.put("mchid", weChatProperties.getMchid());
        // 设置订单描述
        jsonObject.put("description", description);
        // 设置商户系统内部的订单号
        jsonObject.put("out_trade_no", orderNum);
        // 设置微信支付结果通知的URL
        jsonObject.put("notify_url", weChatProperties.getNotifyUrl());

        // 创建JSON对象，用于封装支付金额相关的信息
        JSONObject amount = new JSONObject();
        // 设置订单总金额，转换为分，并取四舍五入
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        // 设置货币类型，此处为人民币
        amount.put("currency", "CNY");

        // 将支付金额信息添加到请求参数中
        jsonObject.put("amount", amount);

        // 创建JSON对象，用于封装支付者的信息
        JSONObject payer = new JSONObject();
        // 设置支付者的openid
        payer.put("openid", openid);

        // 将支付者信息添加到请求参数中
        jsonObject.put("payer", payer);

        // 将请求参数转换为字符串形式
        String body = jsonObject.toJSONString();
        // 发送POST请求到微信支付的JSAPI接口，并返回响应内容
        return post(JSAPI, body);
    }


    /**
     * 小程序支付
     *
     * @param orderNum    商户订单号
     * @param total       金额，单位 元
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return 返回支付所需的JSON对象，如果生成预支付交易单失败，则返回错误信息
     * @throws Exception 如果支付过程中发生异常，则抛出此异常
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        //统一下单，生成预支付交易单
        String bodyAsString = jsapi(orderNum, total, description, openid);
        //解析返回结果
        JSONObject jsonObject = JSON.parseObject(bodyAsString);
        System.out.println(jsonObject);

        //获取预支付交易单的预pay_id
        String prepayId = jsonObject.getString("prepay_id");
        if (prepayId != null) {
            //生成时间戳
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            //生成随机字符串
            String nonceStr = RandomStringUtils.randomNumeric(32);
            //创建列表，用于存储需要签名的参数
            ArrayList<Object> list = new ArrayList<>();
            list.add(weChatProperties.getAppid());
            list.add(timeStamp);
            list.add(nonceStr);
            list.add("prepay_id=" + prepayId);
            //二次签名，调起支付需要重新签名
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : list) {
                stringBuilder.append(o).append("\n");
            }
            String signMessage = stringBuilder.toString();
            byte[] message = signMessage.getBytes();

            //使用SHA256withRSA签名
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath()))));
            signature.update(message);
            String packageSign = Base64.getEncoder().encodeToString(signature.sign());

            //构造数据给微信小程序，用于调起微信支付
            JSONObject jo = new JSONObject();
            jo.put("timeStamp", timeStamp);
            jo.put("nonceStr", nonceStr);
            jo.put("package", "prepay_id=" + prepayId);
            jo.put("signType", "RSA");
            jo.put("paySign", packageSign);

            return jo;
        }
        return jsonObject;
    }

    /**
     * 申请退款
     * @param outTradeNo    商户订单号
     * @param outRefundNo   商户退款单号
     * @param refund        退款金额
     * @param total         原订单金额
     * @return              退款结果
     * @throws Exception    抛出异常
     */
    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        // 创建JSON对象用于封装退款请求参数
        JSONObject jsonObject = new JSONObject();
        // 封装商户订单号和商户退款单号
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("out_refund_no", outRefundNo);

        // 创建JSON对象用于封装金额相关参数
        JSONObject amount = new JSONObject();
        // 计算退款金额和原订单金额，转换为分，并确保两位小数
        amount.put("refund", refund.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        // 设置货币类型为人民币
        amount.put("currency", "CNY");

        // 将金额信息添加到退款请求参数中
        jsonObject.put("amount", amount);
        // 设置退款通知地址
        jsonObject.put("notify_url", weChatProperties.getRefundNotifyUrl());

        // 将请求参数转换为字符串
        String body = jsonObject.toJSONString();

        // 调用申请退款接口
        return post(REFUNDS, body);
    }

}
