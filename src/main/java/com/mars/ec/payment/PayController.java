package com.mars.ec.payment;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Service.CartService;
import com.mars.ec.payment.PayService;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Service.UserService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PayController {

    private final PayService ecPayService;
    private final UserService userService; // 新增：用來找使用者
    private final CartService cartService; // 新增：用來找購物車

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
        return "1|OK";
    }
}