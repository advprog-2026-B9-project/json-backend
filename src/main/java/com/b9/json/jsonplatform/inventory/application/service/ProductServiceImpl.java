package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;

import com.b9.json.jsonplatform.inventory.infrastructure.controller.ProductDetailResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuthService authService;

    @Override
    public Product createProduct(Product product, String ownerUsername) {
        product.setOwnerUsername(ownerUsername);
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(UUID id, Product updatedData, String ownerUsername) throws RuntimeException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        if (!existingProduct.getOwnerUsername().equals(ownerUsername)) {
            throw new RuntimeException("Anda tidak berhak mengubah produk ini");
        }

        existingProduct.setName(updatedData.getName());
        existingProduct.setDescription(updatedData.getDescription());
        existingProduct.setPrice(updatedData.getPrice());
        existingProduct.setStock(updatedData.getStock());

        return productRepository.save(existingProduct);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getMyProducts(String ownerUsername) {
        return productRepository.findByOwner(ownerUsername);
    }

    @Override
    public void deleteProduct(UUID id, String ownerUsername) throws RuntimeException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        if (!product.getOwnerUsername().equals(ownerUsername)) {
            throw new RuntimeException("Anda tidak berhak menghapus produk ini");
        }

        productRepository.deleteById(id);
    }

    @Override
    public List<ProductDetailResponse> getAllProductsWithDetails(String name, String jastiper) {
        List<Product> products = productRepository.searchProducts(name, jastiper);

        return products.stream().map(product -> {
            User user = authService.findByUsername(product.getOwnerUsername());

            String fullName = (user != null) ? user.getFullName() : "Anonim";
            String phone = (user != null) ? user.getPhoneNumber() : "-";

            return new ProductDetailResponse(product, fullName, phone);
        }).toList();
    }
}