package com.mars.ec.product.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.mars.ec.product.Entity.ProductEntity;
import com.mars.ec.product.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

     public ProductEntity addProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }

    public ProductEntity getProductById(Long id) throws Exception{
        Optional<ProductEntity> opt = productRepository.findById(id);
        if(opt.isPresent()){
            return opt.get();
        }
        throw new Exception("Product not found");
    }

    public Page<ProductEntity> getProductsByFilter(String category, Integer minPrice, Integer maxPrice,String sort, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<ProductEntity> products = productRepository.findProductsByFilter(category, minPrice, maxPrice, sort);
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min((startIndex + pageable.getPageSize()), products.size());
        List<ProductEntity> pageContent = products.subList(startIndex, endIndex);
        return new PageImpl<>(pageContent, pageable, products.size());
    }
}
