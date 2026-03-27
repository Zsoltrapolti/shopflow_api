package com.shopflow.service;

import com.shopflow.model.dto.ProductDto;
import com.shopflow.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductService {

    ProductDto.Response createProduct(ProductDto.Request request);

    ProductDto.Response getProductById(Long id);

    Page<ProductDto.Response> getAllProducts(Pageable pageable);

    Page<ProductDto.Response> getProductsByCategory(Product.Category category, Pageable pageable);

    Page<ProductDto.Response> searchProducts(String keyword, Pageable pageable);

    ProductDto.Response updateProduct(Long id, ProductDto.Request request);

    void deleteProduct(Long id);
}
