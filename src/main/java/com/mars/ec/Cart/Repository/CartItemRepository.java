package com.mars.ec.Cart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mars.ec.Cart.Entity.CartEntity;
import com.mars.ec.Cart.Entity.CartItemEntity;
import com.mars.ec.product.Entity.ProductEntity;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.cart = :cart AND ci.product = :product")
    public CartItemEntity isCartItemInCart(@Param("cart") CartEntity cart, @Param("product") ProductEntity product);
}
