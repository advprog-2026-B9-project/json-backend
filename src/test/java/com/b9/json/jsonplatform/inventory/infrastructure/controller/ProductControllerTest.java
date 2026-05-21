package com.b9.json.jsonplatform.inventory.infrastructure.controller;

import com.b9.json.jsonplatform.inventory.application.service.ProductService;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.application.dto.ProductDetailResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product sampleProduct;
    private UUID productId;
    private String ownerUsername;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        ownerUsername = "user1";

        sampleProduct = Product.builder()
                .id(productId)
                .name("produk 1")
                .description("beli bang")
                .price(new BigDecimal("50000"))
                .stock(3)
                .originCountry("Indo")
                .arrivalDate(LocalDate.now().plusDays(7))
                .ownerUsername(ownerUsername)
                .build();
    }

    @Test
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(Product.class), eq(ownerUsername))).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", ownerUsername)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).createProduct(any(Product.class), eq(ownerUsername));
    }

    @Test
    void getAllProducts_WithNoParams_Success() throws Exception {
        ProductDetailResponse response = new ProductDetailResponse(sampleProduct, "User 1", "08123456789");
        List<ProductDetailResponse> responses = Collections.singletonList(response);

        when(productService.getAllProductsWithDetails(null, null)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value(sampleProduct.getName()))
                .andExpect(jsonPath("$[0].jastiperFullName").value("User 1"));

        verify(productService, times(1)).getAllProductsWithDetails(null, null);
    }

    @Test
    void getAllProducts_WithParams_Success() throws Exception {
        ProductDetailResponse response = new ProductDetailResponse(sampleProduct, "User 1", "08123456789");
        List<ProductDetailResponse> responses = Collections.singletonList(response);

        when(productService.getAllProductsWithDetails("produk", ownerUsername)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/products")
                        .param("name", "produk")
                        .param("jastiper", ownerUsername)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value(sampleProduct.getName()))
                .andExpect(jsonPath("$[0].jastiperUsername").value(ownerUsername));

        verify(productService, times(1)).getAllProductsWithDetails("produk", ownerUsername);
    }

    @Test
    void getMyProducts_Success() throws Exception {
        List<Product> products = Collections.singletonList(sampleProduct);
        when(productService.getMyProducts(ownerUsername)).thenReturn(products);

        mockMvc.perform(get("/api/v1/products/me")
                        .header("X-User-Name", ownerUsername)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].ownerUsername").value(ownerUsername));

        verify(productService, times(1)).getMyProducts(ownerUsername);
    }

    @Test
    void getProductById_Success() throws Exception {
        when(productService.getProductById(productId)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void updateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(productId), any(Product.class), eq(ownerUsername))).thenReturn(sampleProduct);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Name", ownerUsername)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).updateProduct(eq(productId), any(Product.class), eq(ownerUsername));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(productId, ownerUsername);

        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                        .header("X-User-Name", ownerUsername))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(productId, ownerUsername);
    }

    @Test
    void deductProductStock_Success() throws Exception {
        doNothing().when(productService).deductProductStock(productId, 2);

        mockMvc.perform(put("/api/v1/products/{id}/deduct-stock", productId)
                        .param("quantity", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).deductProductStock(productId, 2);
    }

    @Test
    void increaseProductStock_Success() throws Exception {
        doNothing().when(productService).increaseProductStock(productId, 3);

        mockMvc.perform(put("/api/v1/products/{id}/increase-stock", productId)
                        .param("quantity", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).increaseProductStock(productId, 3);
    }

    @Test
    void testAdminUpdateProduct_Success() throws Exception {
        when(productService.adminUpdateProduct(eq(productId), any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(put("/api/v1/products/admin/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value(sampleProduct.getName()));

        verify(productService, times(1)).adminUpdateProduct(eq(productId), any(Product.class));
    }

    @Test
    void testAdminDeleteProduct_Success() throws Exception {
        doNothing().when(productService).adminDeleteProduct(productId);

        mockMvc.perform(delete("/api/v1/products/admin/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).adminDeleteProduct(productId);
    }

    @Test
    void addProductRating_Success() throws Exception {
        doNothing().when(productService).addProductRating(productId, 5);

        mockMvc.perform(post("/api/v1/products/{id}/rating", productId)
                        .param("ratingScore", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productService, times(1)).addProductRating(productId, 5);
    }
}