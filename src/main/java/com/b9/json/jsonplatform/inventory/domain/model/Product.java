package com.b9.json.jsonplatform.inventory.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(max = 255, message = "Nama produk terlalu panjang")
    private String name;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Harga harus diisi")
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga tidak boleh negatif")
    private BigDecimal price;

    @NotNull(message = "Stok atau kuota harus diisi")
    @Min(value = 0, message = "Stok minimal adalah 0")
    private Integer stock;

    @NotBlank(message = "Negara atau lokasi asal harus diisi")
    private String originCountry;

    @NotNull(message = "Tanggal pembelian atau kembali harus diisi")
    private LocalDate arrivalDate;

    @NotBlank(message = "Owner username tidak boleh kosong")
    private String ownerUsername;

    @NotNull(message = "Total ulasan tidak boleh null")
    @Min(value = 0, message = "Total ulasan tidak boleh negatif")
    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalReviews = 0;

    @NotNull(message = "Total skor rating tidak boleh null")
    @Min(value = 0, message = "Total skor rating tidak boleh negatif")
    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer totalRatingScore = 0;

    @NotNull(message = "Rata-rata rating tidak boleh null")
    @DecimalMin(value = "0.0", message = "Rata-rata rating minimal adalah 0.0")
    @DecimalMax(value = "5.0", message = "Rata-rata rating maksimal adalah 5.0")
    @Column(nullable = false, precision = 3, scale = 2, columnDefinition = "numeric(3,2) default 0.0")
    @Builder.Default
    private Double averageRating = 0.0;
}