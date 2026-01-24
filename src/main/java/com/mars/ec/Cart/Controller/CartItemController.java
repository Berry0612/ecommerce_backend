package com.mars.ec.Cart.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mars.ec.Cart.Entity.CartItemEntity;
import com.mars.ec.Cart.Service.CartItemService;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.user.Service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cartItem")
public class CartItemController {
    private final CartItemService cartItemService;
    private final UserService userService;

    @PutMapping("/{cartItemId}")
    public ResponseEntity<String> updateCartItem(@PathVariable("cartItemId") Long id,
                                                 @RequestBody CartItemEntity cartItem,
                                                 @RequestHeader("Authorization") String jwt) throws Exception {
        UserEntity user = userService.findUserByJWT(jwt);
        cartItemService.updateCartItem(user.getId(), id, cartItem);

        return new ResponseEntity<>("Cart item updated successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable("cartItemId") Long id,
                                                 @RequestHeader("Authorization") String jwt) throws Exception{
        UserEntity user = userService.findUserByJWT(jwt);
        cartItemService.removeCartItem(user.getId(), id);
        return new ResponseEntity<>("CartItem deleted successfully", HttpStatus.OK);
    }
}
