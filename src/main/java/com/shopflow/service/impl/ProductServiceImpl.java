package com.shopflow.service.impl;

import com.shopflow.exception.DuplicateResourceException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.model.dto.ProductDto;
import com.shopflow.model.entity.Product;
import com.shopflow.repository.ProductRepository;
import com.shopflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product service implementation.
 *
 * <p>Single Responsibility: handles only product business logic.
 * All DB access is delegated to the repository layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductDto.Response createProduct(ProductDto.Request request) {
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Product", "name", request.getName());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .build();

        Product saved = productRepository.save(product);
        log.info("Created product #{}: {}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    public ProductDto.Response getProductById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public Page<ProductDto.Response> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<ProductDto.Response> getProductsByCategory(Product.Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable).map(this::toResponse);
    }

    @Override
    public Page<ProductDto.Response> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByName(keyword, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public ProductDto.Response updateProduct(Long id, ProductDto.Request request) {
        Product product = findOrThrow(id);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());

        log.info("Updated product #{}", id);
        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
        log.info("Deleted product #{}", id);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductDto.Response toResponse(Product p) {
        return ProductDto.Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .category(p.getCategory())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
