package com.mars.ec.user.Controller;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Service.CartService;
import com.mars.ec.config.JWTProvider;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandle(@RequestBody UserEntity userEntity) throws Exception {

        userService.createUserEntity(userEntity);

        String token = jwtProvider.generateToken(userEntity.getEmail());
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

        String token = jwtProvider.generateToken(foundUserEntity.getEmail());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(token);
        authResponse.setMessage("登入成功");
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

}