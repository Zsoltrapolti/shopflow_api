package com.shopflow.service;

import com.shopflow.exception.DuplicateResourceException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.model.dto.ProductDto;
import com.shopflow.model.entity.Product;
import com.shopflow.repository.ProductRepository;
import com.shopflow.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductDto.Request sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Sony Headphones")
                .description("Premium noise-cancelling")
                .price(new BigDecimal("299.99"))
                .stockQuantity(50)
                .category(Product.Category.ELECTRONICS)
                .build();
        // Simulate @PrePersist
        sampleProduct.setCreatedAt(Instant.now());
        sampleProduct.setUpdatedAt(Instant.now());

        sampleRequest = ProductDto.Request.builder()
                .name("Sony Headphones")
                .description("Premium noise-cancelling")
                .price(new BigDecimal("299.99"))
                .stockQuantity(50)
                .category(Product.Category.ELECTRONICS)
                .build();
    }

    @Nested
    @DisplayName("createProduct()")
    class CreateProduct {

        @Test
        @DisplayName("should create and return product when name is unique")
        void shouldCreateProduct() {
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

            ProductDto.Response result = productService.createProduct(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Sony Headphones");
            assertThat(result.getPrice()).isEqualByComparingTo("299.99");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when name already exists")
        void shouldThrowOnDuplicateName() {
            when(productRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(sampleRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Sony Headphones");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getProductById()")
    class GetProductById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            ProductDto.Response result = productService.getProductById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCategory()).isEqualTo(Product.Category.ELECTRONICS);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProduct {

        @Test
        @DisplayName("should update all fields and return updated product")
        void shouldUpdateProduct() {
            ProductDto.Request updateRequest = ProductDto.Request.builder()
                    .name("Sony Headphones V2")
                    .price(new BigDecimal("319.99"))
                    .stockQuantity(40)
                    .category(Product.Category.ELECTRONICS)
                    .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProductDto.Response result = productService.updateProduct(1L, updateRequest);

            assertThat(result.getName()).isEqualTo("Sony Headphones V2");
            assertThat(result.getPrice()).isEqualByComparingTo("319.99");
        }
    }

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProduct {

        @Test
        @DisplayName("should delete product when it exists")
        void shouldDeleteProduct() {
            when(productRepository.existsById(1L)).thenReturn(true);

            assertThatCode(() -> productService.deleteProduct(1L))
                    .doesNotThrowAnyException();

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenNotFound() {
            when(productRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> productService.deleteProduct(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).deleteById(any());
        }
    }
}
