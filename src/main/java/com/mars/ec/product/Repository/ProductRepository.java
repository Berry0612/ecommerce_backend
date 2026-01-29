package com.mars.ec.product.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.mars.ec.product.Entity.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

   @Query(
   "SELECT p FROM ProductEntity p " +
   "WHERE (p.status = true) " +
   "AND (p.category = :category OR :category LIKE '') " +
   "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
   "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
   "ORDER BY " +
   "CASE :sort WHEN 'price_low' THEN p.price END ASC, " +
   "CASE :sort WHEN 'price_high' THEN p.price END DESC")

   public List<ProductEntity> findProductsByFilter(
      @Param("category") String category,
      @Param("minPrice") Integer minPrice,
      @Param("maxPrice") Integer maxPrice,
      @Param("sort") String sort);
      
}

