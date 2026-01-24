package com.mars.ec.user.Controller;

import java.util.List;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Service.ProductService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final ProductService productService;
  
  
    @PostMapping("/")
    public ResponseEntity<List<ProductEntity>> addProducts(@RequestBody ProductEntity[] products) {
        List<ProductEntity> createdProducts = new ArrayList<>();
        for(ProductEntity product : products) {
            ProductEntity p = productService.addProduct(product);
            createdProducts.add(p);
        }
        return new ResponseEntity<>(createdProducts, HttpStatus.CREATED);
    }

}
