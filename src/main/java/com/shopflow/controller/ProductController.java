package com.shopflow.controller;

import com.shopflow.model.dto.ProductDto;
import com.shopflow.model.entity.Product;
import com.shopflow.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Product REST controller.
 *
 * <p>Thin layer — delegates all logic to {@link ProductService}.
 * Handles only HTTP concerns: routing, status codes, request binding.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalogue management")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductDto.Response create(@Valid @RequestBody ProductDto.Request request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ProductDto.Response getById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping
    @Operation(summary = "List all products (paginated)")
    public Page<ProductDto.Response> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return productService.getAllProducts(PageRequest.of(page, size, Sort.by(sortBy)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter products by category")
    public Page<ProductDto.Response> getByCategory(
            @PathVariable Product.Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getProductsByCategory(category, PageRequest.of(page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name keyword")
    public Page<ProductDto.Response> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.searchProducts(keyword, PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ProductDto.Response update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.Request request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    public void delete(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
