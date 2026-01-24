package com.mars.ec.Cart.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.mars.ec.Cart.Entity.CartEntity;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long>{
    @Query("SELECT c FROM CartEntity c WHERE c.user.id = :userId")
    public CartEntity findByUserId(@Param("userId") Long userId);
}

