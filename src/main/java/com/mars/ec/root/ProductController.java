// package com.mars.ec.root;

// import org.springframework.data.domain.Page;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import com.mars.ec.product.Entity.ProductEntity;
// import com.mars.ec.product.Service.ProductService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.http.HttpStatus;



// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/product")
// public class ProductController {
//     private final ProductService productService;

//     @PreAuthorize("hasRole('ADMIN')")
//     @PostMapping("/")
//     public ResponseEntity<ProductEntity> addProduct(@RequestBody ProductEntity product) {
//         ProductEntity createdProduct = productService.addProduct(product);
//         return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<String> deleteProduct(@PathVariable("id") Long id) {
//         productService.deleteProduct(id); 
//         return new ResponseEntity<>(HttpStatus.OK);
//     }
// }
