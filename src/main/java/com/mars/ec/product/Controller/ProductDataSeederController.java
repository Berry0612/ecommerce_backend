package com.mars.ec.product.Controller; // 或是放在適合的 package

import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Repository.ProductRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/seed/product")
public class ProductDataSeederController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/")
    public ResponseEntity<String> seedProducts(@RequestParam(defaultValue = "1000") int count) {
        Faker faker = new Faker(new Locale("zh-TW")); // 使用繁體中文數據
        List<ProductEntity> productList = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            ProductEntity product = new ProductEntity();
            // 根據你的 ProductEntity 欄位
            product.setName(faker.commerce().productName());
            product.setDescription(faker.lorem().sentence(10)); // 隨機描述
            product.setPrice(Integer.parseInt(faker.commerce().price().replaceAll("[^\\d.]", "").split("\\.")[0])); // 轉為整數價格
            product.setCategory(faker.commerce().department());
            product.setStatus(true);

            int randomImageId = faker.number().numberBetween(1, 1000);
            product.setImageUrl("https://picsum.photos/id/" + randomImageId + "/300/300");

            productList.add(product);

            // 每 1000 筆儲存一次，避免記憶體溢出
            if (productList.size() >= 1000) {
                productRepository.saveAll(productList);
                productList.clear();
            }
        }

        // 儲存剩餘的資料
        if (!productList.isEmpty()) {
            productRepository.saveAll(productList);
        }

        long endTime = System.currentTimeMillis();
        return ResponseEntity.ok("成功生成 " + count + " 筆商品資料，耗時: " + (endTime - startTime) + "ms");
    }
}
