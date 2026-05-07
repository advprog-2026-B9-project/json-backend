package com.b9.json.jsonplatform.inventory.infrastructure.controller;
import com.b9.json.jsonplatform.inventory.domain.model.Product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProductDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String originCountry;
    private LocalDate arrivalDate;

    private String jastiperUsername;
    private String jastiperFullName;
    private String jastiperPhoneNumber;

    public ProductDetailResponse(Product product, String fullName, String phoneNumber) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.originCountry = product.getOriginCountry();
        this.arrivalDate = product.getArrivalDate();

        this.jastiperUsername = product.getOwnerUsername();
        this.jastiperFullName = fullName;
        this.jastiperPhoneNumber = phoneNumber;
    }
}