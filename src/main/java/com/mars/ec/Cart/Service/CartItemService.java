package com.mars.ec.Cart.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Entity.CartItemEntity;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.user.Entity.UserEntity;
import com.mars.ec.Cart.Repository.CartItemRepository;
import com.mars.ec.user.Service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final UserService userService;

     public CartItemEntity isCartItemInCart(CartEntity cart, ProductEntity product) {
        return cartItemRepository.isCartItemInCart(cart, product);
    }

    public CartItemEntity createCartItem(CartItemEntity cartItem) {
        cartItem.setQuantity(Math.max(cartItem.getQuantity(), 1));
        cartItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    public CartItemEntity updateCartItem(Long userId, Long id, CartItemEntity cartItem) throws Exception {
        CartItemEntity item = findCartItemById(id);
        UserEntity user = userService.findUserById(item.getCart().getUser().getId());
        if(user.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(item.getQuantity() * item.getProduct().getPrice());
        }

        return cartItemRepository.save(item);
    }
   
    public CartItemEntity findCartItemById(Long id) throws Exception {
        Optional<CartItemEntity> optionalCartItem = cartItemRepository.findById(id);
        if(optionalCartItem.isPresent()) {
            return optionalCartItem.get();
        }
        throw new Exception("CartItem not found with id : " + id);
    }
    public void removeCartItem(Long userId, Long id) throws Exception {
        CartItemEntity item = findCartItemById(id);    //getCart() --> Cartitem
        UserEntity user = userService.findUserById(item.getCart().getUser().getId());
        UserEntity reqUser = userService.findUserById(userId);
        if(user.getId().equals(reqUser.getId())) {
            cartItemRepository.deleteById(id);
            return;
        }
        throw new Exception("Can't remove another users item");
    }
}
