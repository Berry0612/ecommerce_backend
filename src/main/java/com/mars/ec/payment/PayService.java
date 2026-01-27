package com.mars.ec.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.codec.digest.DigestUtils;

@Service
public class PayService {

    @Value("${ecpay.merchant-id}")
    private String merchantId;

    @Value("${ecpay.hash-key}")
    private String hashKey;

    @Value("${ecpay.hash-iv}")
    private String hashIv;

    @Value("${ecpay.payment-url}")
    private String paymentUrl;

    @Value("${ecpay.return-url}")
    private String returnUrl;
    
    @Value("${ecpay.client-back-url}")
    private String clientBackUrl;

    // 產生給前端的 HTML Form
    public String createECPayOrder(String orderId, Integer totalAmount, String itemName) {
        
        // 1. 設定傳送給綠界的參數
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", orderId);
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount));
        params.put("TradeDesc", "ECPay Integration Test");
        params.put("ItemName", itemName);
        params.put("ReturnURL", returnUrl); // 後端 callback
        params.put("ClientBackURL", clientBackUrl); // 付款後跳轉回前端
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        // 2. 計算 CheckMacValue
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 3. 產生 Auto-Submit 的 HTML Form
        StringBuilder html = new StringBuilder();
        html.append("<form id='ecpay-form' action='").append(paymentUrl).append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("' />");
        }
        // 自動送出腳本
        html.append("</form>");
        html.append("<script>document.getElementById('ecpay-form').submit();</script>");

        return html.toString();
    }

    // 綠界加密邏輯: 1. 排序 2. 串接 Key/IV 3. URL Encode 4. 轉小寫 5. SHA256 6. 轉大寫
    private String generateCheckMacValue(TreeMap<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(hashKey);
            for (String key : params.keySet()) {
                sb.append("&").append(key).append("=").append(params.get(key));
            }
            sb.append("&HashIV=").append(hashIv);

            String urlEncoded = URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8.name())
                    .toLowerCase()
                    .replace("%21", "!")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%2a", "*");

            return DigestUtils.sha256Hex(urlEncoded).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue generation failed", e);
        }
    }
}