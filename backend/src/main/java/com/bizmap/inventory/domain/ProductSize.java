package com.bizmap.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_sizes")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    public enum Size {
        XS, S, M, L, XL, XXL
    }
}
