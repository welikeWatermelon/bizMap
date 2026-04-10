package com.bizmap.inventory.repository;

import com.bizmap.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
