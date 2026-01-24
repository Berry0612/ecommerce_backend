package com.mars.ec.Cart.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mars.ec.Cart.AddItemRequest;
import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Service.CartService;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<CartEntity> findUserCart(@RequestHeader("Authorization") String jwt) throws Exception {
        UserEntity user = userService.findUserByJWT(jwt);
        CartEntity cart = cartService.calcCartTotal(user.getId());
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/add")
    public ResponseEntity<String> addItemToCart(@RequestBody AddItemRequest req, @RequestHeader("Authorization") String jwt) throws Exception{
        UserEntity user = userService.findUserByJWT(jwt);
        cartService.addToCart(user.getId(), req);
        return new ResponseEntity<>("Item added to cart",HttpStatus.OK);
    }
}
