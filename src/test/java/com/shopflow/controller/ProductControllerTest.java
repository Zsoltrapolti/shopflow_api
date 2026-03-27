package com.shopflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.model.dto.ProductDto;
import com.shopflow.model.entity.Product;
import com.shopflow.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.shopflow.config.SecurityConfig;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private ProductDto.Response buildResponse(Long id, String name) {
        return ProductDto.Response.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("299.99"))
                .stockQuantity(50)
                .category(Product.Category.ELECTRONICS)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 with product when found")
        void shouldReturn200() throws Exception {
            when(productService.getProductById(1L)).thenReturn(buildResponse(1L, "Sony Headphones"));

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Sony Headphones"))
                    .andExpect(jsonPath("$.price").value(299.99));
        }

        @Test
        @DisplayName("should return 404 when product not found")
        void shouldReturn404() throws Exception {
            when(productService.getProductById(99L))
                    .thenThrow(new ResourceNotFoundException("Product", 99L));

            mockMvc.perform(get("/api/v1/products/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Resource Not Found"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProduct {

        @Test
        @WithMockUser
        @DisplayName("should return 201 with created product")
        void shouldReturn201() throws Exception {
            ProductDto.Request request = ProductDto.Request.builder()
                    .name("Sony Headphones")
                    .price(new BigDecimal("299.99"))
                    .stockQuantity(50)
                    .category(Product.Category.ELECTRONICS)
                    .build();

            when(productService.createProduct(any())).thenReturn(buildResponse(1L, "Sony Headphones"));

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when request is invalid")
        void shouldReturn400OnInvalidRequest() throws Exception {
            ProductDto.Request invalid = ProductDto.Request.builder()
                    .name("") // blank name — should fail validation
                    .price(new BigDecimal("-5.00")) // negative price
                    .stockQuantity(-1)
                    .build();

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @WithMockUser
        @DisplayName("should return 204 on successful deletion")
        void shouldReturn204() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc.perform(delete("/api/v1/products/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
