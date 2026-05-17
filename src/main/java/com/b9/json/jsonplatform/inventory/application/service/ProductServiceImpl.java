package com.b9.json.jsonplatform.inventory.application.service;

import com.b9.json.jsonplatform.auth.application.service.AuthService;
import com.b9.json.jsonplatform.auth.domain.User;
import com.b9.json.jsonplatform.inventory.application.exception.InsufficientStockException;
import com.b9.json.jsonplatform.inventory.application.exception.InvalidStockQuantityException;
import com.b9.json.jsonplatform.inventory.application.exception.ProductNotFoundException;
import com.b9.json.jsonplatform.inventory.application.exception.ProductOwnershipException;
import com.b9.json.jsonplatform.inventory.domain.model.Product;
import com.b9.json.jsonplatform.inventory.domain.repository.ProductRepository;
import com.b9.json.jsonplatform.inventory.application.dto.ProductDetailResponse;

import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Product updateProduct(UUID id, Product updatedData, String ownerUsername) {
        Product existingProduct = findProductByIdForUpdate(id);
        validateOwnership(existingProduct.getOwnerUsername(), ownerUsername);

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
    @Transactional
    public void deleteProduct(UUID id, String ownerUsername) {
        Product product = findProductByIdForUpdate(id);
        validateOwnership(product.getOwnerUsername(), ownerUsername);

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

    @Override
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));
    }

    @Override
    @Transactional
    public void deductProductStock(UUID id, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidStockQuantityException("Jumlah pengurangan stok harus lebih dari 0");
        }

        Product product = findProductByIdForUpdate(id);
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Stok tidak mencukupi untuk produk: %s. Sisa stok: %d"
                            .formatted(product.getName(), product.getStock())
            );
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void increaseProductStock(UUID id, Integer quantity) throws RuntimeException {
        if (quantity == null || quantity <= 0) {
            throw new InvalidStockQuantityException("Jumlah penambahan stok harus lebih dari 0");
        }

        Product product = findProductByIdForUpdate(id);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void adminDeleteProduct(UUID id) {
        Product product = findProductByIdForUpdate(id);
        productRepository.deleteById(product.getId());
    }

    @Override
    @Transactional
    public Product adminUpdateProduct(UUID id, Product updatedProduct) {
        Product existingProduct = findProductByIdForUpdate(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setOriginCountry(updatedProduct.getOriginCountry());
        existingProduct.setArrivalDate(updatedProduct.getArrivalDate());

        return productRepository.save(existingProduct);
    }

    private void validateOwnership(String productOwner, String requesterUsername) {
        if (!productOwner.equals(requesterUsername)) {
            throw new ProductOwnershipException("Anda tidak berhak memodifikasi produk ini");
        }
    }

    private Product findProductByIdForUpdate(UUID id){
        return productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ProductNotFoundException("Produk tidak ditemukan"));
    }
}