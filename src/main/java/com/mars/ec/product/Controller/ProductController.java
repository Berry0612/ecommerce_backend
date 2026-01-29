package com.mars.ec.product.Controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/")
    //value用來定位URL中的參數，required = false代表可以不填，required = true是強制要填資料
    public ResponseEntity<Page<ProductEntity>> findProductByFilter(

        @RequestParam(value = "category", required = false, defaultValue = "") String category,                                                    
        @RequestParam(value = "minPrice", required = false) Integer minPrice,                                                   
        @RequestParam(value = "maxPrice", required = false) Integer maxPrice,                                                  
        @RequestParam(value = "sort", required = false) String sort,                         
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,                                               
        @RequestParam(value = "pageSize", required = false, defaultValue = "9") Integer pageSize){

        Page<ProductEntity> filteredProductsPage = productService.getProductsByFilter(category, minPrice, maxPrice, sort, pageNumber, pageSize);
        return new ResponseEntity<>(filteredProductsPage, HttpStatus.OK);
    }
    // -----------------------------------------------------
    @PostMapping("/")
    public ResponseEntity<ProductEntity> addProduct(@RequestBody ProductEntity product) {
        ProductEntity createdProduct = productService.addProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable("id") Long id) throws Exception {
        try {
            return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    } 

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
        productService.changeStatus(id); //沒有刪掉只是改變上下架狀態
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
    
