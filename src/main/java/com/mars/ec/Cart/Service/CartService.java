package com.mars.ec.Cart.Service;

import com.mars.ec.user.Entity.UserEntity;
import lombok.RequiredArgsConstructor;
import java.util.Iterator;

import org.springframework.stereotype.Service;

import com.mars.ec.Cart.AddItemRequest;
import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Entity.CartItemEntity;
import com.mars.ec.Cart.Repository.CartRepository;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Service.ProductService;
import com.mars.ec.user.Service.UserService;

@Service
@RequiredArgsConstructor
public class CartService {
private final CartRepository cartRepository;
private final CartItemService cartItemService;
private final ProductService productService;
private final UserService userService;

    public CartEntity createCart(UserEntity user) {
        CartEntity cart = new CartEntity();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    public void addToCart(Long userId, AddItemRequest req) throws Exception{
        CartEntity cart = cartRepository.findByUserId(userId);
        // 如果找不到購物車，就幫他建立一個新的！
        if (cart == null) {
            cart = new CartEntity();
            // 這裡需要 UserEntity，建議把 userId 改傳 user 物件進來，或是這裡再查一次 User
            UserEntity user = userService.findUserById(userId); 
            cart.setUser(user);
            cart = cartRepository.save(cart); // 存檔後，cart 就不會是 null 了
        }
        //
        ProductEntity product = productService.getProductById(req.getProductId());
        CartItemEntity isPresent = cartItemService.isCartItemInCart(cart, product);
        if(isPresent == null) {
            CartItemEntity cartItem = new CartItemEntity();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(req.getQuantity());
            cartItem.setPrice(req.getQuantity() * product.getPrice());

            CartItemEntity createdCartItem = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(createdCartItem);
            
            calcCartTotal(userId);
            
        }
    }

    public Integer clearCart(Long userId) throws Exception {
        CartEntity cart = cartRepository.findByUserId(userId);
        Integer totalPrice = cart.getTotalPrice();

        Iterator<CartItemEntity> iterator = cart.getCartItems().iterator();
        while (iterator.hasNext()) {
            CartItemEntity cartItem = iterator.next();
            cartItemService.removeCartItem(userId, cartItem.getId());
            iterator.remove();
        }

        cart.setTotalPrice(0);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);

        return totalPrice;
    }

    public CartEntity calcCartTotal(Long userId) {
        CartEntity cart = cartRepository.findByUserId(userId);
        int totalPrice = 0, totalQuantity = 0;

        for(CartItemEntity cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getPrice();
            totalQuantity += cartItem.getQuantity();
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
        return cartRepository.save(cart);
    }

}
