package com.bizmap.inventory.repository;

import com.bizmap.inventory.domain.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {

    List<ProductSize> findAllByProductId(Long productId);
}
