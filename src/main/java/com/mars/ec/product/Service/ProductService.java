package com.mars.ec.product.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final int PRODUCT_REDIS_CACHE_MINUTES = 1;
    private final Random random = new Random();

    public ProductEntity addProduct(ProductEntity product) {
        product.setStatus(true);
        return productRepository.save(product);
    }

    //控制上下架邏輯
    public void changeStatus(Long id){
        Optional<ProductEntity> optProduct = productRepository.findById(id);
        if(optProduct.isPresent()){
            ProductEntity product =  optProduct.get();
            product.setStatus(false);
            productRepository.save(product);

            String cacheKey = "product:" + id;
            redisTemplate.delete(cacheKey);
        }
    }

    public ProductEntity getProductById(Long id) throws Exception {
        String cacheKey = "product:" + id;
        ProductEntity cachedProduct = (ProductEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            if (Boolean.TRUE.equals(cachedProduct.getStatus())){
                return cachedProduct;
            }
            else{
                redisTemplate.delete(cacheKey);
            }
        }
        // 沒有在Redis快取中，必須從資料庫取得
        Optional<ProductEntity> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            ProductEntity product = opt.get();
            if(Boolean.FALSE.equals(product.getStatus())){
                throw new Exception("Product not found");
            }
            // 存入Redis快取，設定過期規則
            int random_delay = random.nextInt(3);
            redisTemplate.opsForValue().set(cacheKey, product, PRODUCT_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
            return product;
        }
        throw new Exception("Product not found");
    }

    // 請將 ProductService.java 內的此方法完全替換
public Page<ProductEntity> getProductsByFilter(String category, Integer minPrice, Integer maxPrice, String sort, Integer pageNumber, Integer pageSize) {
    
    // 1. 使用新變數處理空字串，保持參數不變
    String filterCategory = category;
    if (filterCategory != null && filterCategory.trim().isEmpty()) {
        filterCategory = null;
    }

    // 2. 建立快取 Key
    String cacheKey = "products:v2:cat:" + category + ":min:" + minPrice + ":max:" + maxPrice + ":sort:" + sort + ":p:" + pageNumber;

    // 3. 嘗試讀取快取
    try {
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return new PageImpl<>((List<ProductEntity>) cached, PageRequest.of(pageNumber, pageSize), ((List<?>) cached).size());
        }
    } catch (Exception e) {
        System.err.println("Redis error: " + e.getMessage());
    }

    // 4. [關鍵點] 使用 Specification 進行動態查詢
    // 不要再用 productRepository.findProductsByFilter，因為它處理不了 null
    org.springframework.data.jpa.domain.Specification<ProductEntity> spec = (root, query, cb) -> {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        // 只有 category 有值才過濾，是 null 就不會出現在 SQL 條件裡
        if (category != null) {
            predicates.add(cb.equal(root.get("category"), category));
        }

        // 價格區間判斷
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // 只顯示上架商品
        predicates.add(cb.equal(root.get("status"), true));

        return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    // 5. 處理排序
    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").descending());
    if ("price_asc".equals(sort)) {
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
    } else if ("price_desc".equals(sort)) {
        pageable = PageRequest.of(pageNumber, pageSize, Sort.by("price").descending());
    }

    // 6. 執行查詢
    Page<ProductEntity> resultPage = productRepository.findAll(spec, pageable);

    // 7. 存入快取
    if (!resultPage.isEmpty()) {
        redisTemplate.opsForValue().set(cacheKey, resultPage.getContent(), 1, TimeUnit.MINUTES);
    }

    return resultPage;
}
}