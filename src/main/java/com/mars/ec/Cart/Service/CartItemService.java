package com.mars.ec.Cart.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final int CARTITEM_REDIS_CACHE_MINUTES = 1;
    private final Random random = new Random();

    public CartItemEntity isCartItemInCart(CartEntity cart, ProductEntity product) {
        return cartItemRepository.isCartItemInCart(cart, product);
    }

    public CartItemEntity createCartItem(CartItemEntity cartItem) {
        cartItem.setQuantity(Math.max(cartItem.getQuantity(), 1));
        cartItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());

        return cartItemRepository.save(cartItem);
    }
   
    // 修改處：更新時刪除快取
    public CartItemEntity updateCartItem(Long userId, Long id, CartItemEntity cartItem) throws Exception {
        CartItemEntity item = findCartItemById(id);
        UserEntity user = userService.findUserById(item.getCart().getUser().getId());
        
        if (user.getId().equals(userId)) {
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(item.getQuantity() * item.getProduct().getPrice());

            // 刪除快取
            String cacheKey = "cartItem:" + id;
            redisTemplate.delete(cacheKey);
        }

        return cartItemRepository.save(item);
    }

    // 修改處：查詢加入快取
    public CartItemEntity findCartItemById(Long id) throws Exception {
        // 尋找快取
        String cacheKey = "cartItem:" + id;
        CartItemEntity cachedCartItem = (CartItemEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedCartItem != null) {
            return cachedCartItem;
        }
        
        // cache miss
        Optional<CartItemEntity> optionalCartItem = cartItemRepository.findById(id);
        if (optionalCartItem.isPresent()) {
            CartItemEntity cartItem = optionalCartItem.get();
            int random_delay = random.nextInt(3);
            redisTemplate.opsForValue().set(cacheKey, cartItem, CARTITEM_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
            return cartItem;
        }
        throw new Exception("CartItem not found with id : " + id);
    }

    // 修改處：刪除時清除快取
    public void removeCartItem(Long userId, Long id) throws Exception {
        CartItemEntity item = findCartItemById(id);
        UserEntity user = userService.findUserById(item.getCart().getUser().getId());
        UserEntity reqUser = userService.findUserById(userId);
        
        if (user.getId().equals(reqUser.getId())) {
            cartItemRepository.deleteById(id);
            // 刪除快取
            String cacheKey = "cartItem:" + id;
            redisTemplate.delete(cacheKey);
            return;
        }
        throw new Exception("Can't remove another users item");
    }
}
