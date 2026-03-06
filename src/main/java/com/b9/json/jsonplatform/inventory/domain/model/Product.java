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
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private String ownerUsername;
}