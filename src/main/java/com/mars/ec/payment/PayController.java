package com.mars.ec.payment;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Service.CartService;
import com.mars.ec.payment.PayService;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Service.UserService;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayController {

    private final PayService ecPayService;
    private final UserService userService; // 新增：用來找使用者
    private final CartService cartService; // 新增：用來找購物車
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 前端按下「結帳」時呼叫此 API
    @PostMapping("/checkout")
    public String checkout(@RequestHeader("Authorization") String jwt) {
        try {
            // 1. 透過 JWT 找出目前登入的是誰
            UserEntity user = userService.findUserByJWT(jwt);
            
            // 2. 找出該使用者的購物車，並確保金額是最新的
            CartEntity cart = cartService.calcCartTotal(user.getId());
            
            // 檢查購物車是否為空
            if (cart.getTotalPrice() == null || cart.getTotalPrice() == 0) {
                return "Error: Cart is empty";
            }

            // 3. 使用真實數據產生訂單
            // 訂單編號: 使用 UUID 避免重複，或是 "EC" + userId + timestamp
            String orderId = "EC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            
            // 真實金額
            Integer totalAmount = cart.getTotalPrice();
            
            // 商品名稱 (綠界限制長度，這裡簡單顯示就好)
            String itemName = "Mars EC 購物車商品 (共 " + cart.getTotalQuantity() + " 件)";

            // 4. 回傳綠界需要的 HTML Form
            return ecPayService.createECPayOrder(orderId, totalAmount, itemName);

        } catch (Exception e) {
            e.printStackTrace();
            return "Checkout Failed: " + e.getMessage();
        }
    }
    
    @PostMapping("/callback")
    public String callback(@RequestParam java.util.Map<String, String> params) {
        System.out.println("收到綠界付款通知: " + params);
        // TODO: 在這裡驗證 CheckMacValue 並更新資料庫訂單狀態 (ex: cart clear)
        String orderId = params.get("MerchantTradeNo");
        String rtnCode = params.get("RtnCode"); // 1 代表成功

        // 1. 基本檢查：確認交易是否成功
        if (!"1".equals(rtnCode)) {
            return "1|OK"; // 雖然失敗，但也回傳 OK 讓綠界不要再一直通知了
        }

        // 2. 利用 Redis 實作冪等性 (Idempotency)
        // 邏輯：嘗試在 Redis 設定一個 key，如果 key 已經存在 (setIfAbsent 回傳 false)，代表這筆訂單已經處理過了
        String redisKey = "order:processed:" + orderId;
        
        // setIfAbsent 等同於 Redis 指令: SETNX (Set if Not eXists)
        // 我們設定 24 小時過期，避免 Redis 塞滿舊資料
        Boolean isFirstProcess = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSED", 1, TimeUnit.DAYS);

        if (Boolean.FALSE.equals(isFirstProcess)) {
            System.out.println("⚠️ 重複的付款通知，跳過處理: " + orderId);
            return "1|OK"; // 直接告訴綠界我收到了，不用再傳了
        }

        // 3. 只有第一次進來的請求會走到這裡
        try {
            // TODO: 驗證 CheckMacValue (非常重要，防止偽造)
            // boolean isValid = ecPayService.verifyCheckMacValue(params);
            
            // TODO: 更新資料庫訂單狀態 (ex: order.setStatus("PAID"))
            // TODO: 清空使用者購物車
            
            System.out.println("✅ 訂單處理成功: " + orderId);
            return "1|OK";

        } catch (Exception e) {
            // 如果處理過程報錯，我們把 Redis key 刪掉，讓綠界下次重試時可以再次進入處理
            redisTemplate.delete(redisKey);
            e.printStackTrace();
            return "0|Error"; // 回傳錯誤，讓綠界稍後重試
        }
    }
}