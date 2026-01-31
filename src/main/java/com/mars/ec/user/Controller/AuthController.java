package com.mars.ec.user.Controller;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Service.CartService;
import com.mars.ec.config.JWTProvider;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Response.AuthResponse;
import com.mars.ec.user.Service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final JWTProvider jwtProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;
    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandle(@RequestBody UserEntity userEntity) throws Exception {

        userService.createUserEntity(userEntity);

        String token = jwtProvider.generateToken(userEntity.getEmail(), userEntity.getRole());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("註冊成功");
        CartEntity cart = cartService.createCart(userService.findUserByEmail(userEntity.getEmail())); //新增購物車
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED); 
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginHandle(@RequestBody UserEntity userEntity) throws Exception {

        UserEntity foundUserEntity = userService.findUserByEmail(userEntity.getEmail());

        if (foundUserEntity == null
                || !passwordEncoder.matches(userEntity.getPassword(), foundUserEntity.getPassword())) {
            throw new Exception("信箱或密碼錯誤");
        }

        String token = jwtProvider.generateToken(foundUserEntity.getEmail(), foundUserEntity.getRole());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("登入成功");
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String jwt) {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7); // 去掉 "Bearer "
        }
        
        try {
            // 1. 計算 Token 還有多久過期
            java.util.Date expirationDate = jwtProvider.getExpirationDateFromJWT(jwt);
            long now = System.currentTimeMillis();
            long ttl = expirationDate.getTime() - now; // 剩餘毫秒數

            // 2. 如果還沒過期，就加入 Redis 黑名單
            if (ttl > 0) {
                // Key: "blacklist:token字串", Value: "logout", 過期時間: ttl
                redisTemplate.opsForValue().set("blacklist:" + jwt, "logout", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // 如果 Token 已經過期或無效，這裡會報錯，但我們可以直接忽略
            // 因為無效的 Token 本來就不能用了，視為登出成功即可
        }

        return new ResponseEntity<>("登出成功", HttpStatus.OK);
    }
}