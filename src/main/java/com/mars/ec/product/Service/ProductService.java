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


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final int PRODUCT_REDIS_CACHE_MINUTES = 1;
    private final Random random = new Random();

     public ProductEntity addProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        // 刪除Redis快取
        String cacheKey = "product:" + id;
        redisTemplate.delete(cacheKey);
    }

    public ProductEntity getProductById(Long id) throws Exception {
        // 從Redis取得快取資料
        String cacheKey = "product:" + id;
        ProductEntity cachedProduct = (ProductEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cachedProduct != null) {
            return cachedProduct;
        }
        // 沒有在Redis快取中，必須從資料庫取得
        Optional<ProductEntity> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            ProductEntity product = opt.get();
            // 存入Redis快取，設定過期規則
            int random_delay = random.nextInt(3);
            redisTemplate.opsForValue().set(cacheKey, product, PRODUCT_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
            return product;
        }
        throw new Exception("Product not found");
    }

    // 修改處：列表查詢加入快取
    public Page<ProductEntity> getProductsByFilter(String category, Integer minPrice, Integer maxPrice, String sort, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<ProductEntity> products;

        // 在快取中尋找
        String cacheKey = "products:filter:category:" + category +
                ":minPrice:" + minPrice +
                ":maxPrice:" + maxPrice +
                ":sort:" + sort +
                ":page:" + pageNumber +
                ":size:" + pageSize;
        
        List<ProductEntity> cachedProducts = (List<ProductEntity>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedProducts != null) {
            // 直接將快取的資料放入products
            products = cachedProducts;
        } else {
            // 沒在快取中，從資料庫取得符合條件的產品
            products = productRepository.findProductsByFilter(category, minPrice, maxPrice, sort);
            int random_delay = random.nextInt(3);
            redisTemplate.opsForValue().set(cacheKey, products, PRODUCT_REDIS_CACHE_MINUTES + random_delay, TimeUnit.MINUTES);
        }

        // 設定從哪裡開始取資料，哪裡結束 (因為快取存的是完整List，需要手動分頁)
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min((startIndex + pageable.getPageSize()), products.size());

        if (startIndex > products.size()) {
             return new PageImpl<>(List.of(), pageable, products.size());
        }

        // 從過濾後的產品列表，截取對應頁數和數量的產品
        List<ProductEntity> pageContent = products.subList(startIndex, endIndex);

        return new PageImpl<>(pageContent, pageable, products.size());
    }
}
