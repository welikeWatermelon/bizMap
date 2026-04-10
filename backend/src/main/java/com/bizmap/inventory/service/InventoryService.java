package com.bizmap.inventory.service;

import com.bizmap.common.exception.BizMapException;
import com.bizmap.common.exception.ErrorCode;
import com.bizmap.inventory.dto.ProductResponse;
import com.bizmap.inventory.dto.SizeResponse;
import com.bizmap.inventory.dto.StoreSearchResponse;
import com.bizmap.inventory.repository.ProductRepository;
import com.bizmap.inventory.repository.ProductSizeRepository;
import com.bizmap.inventory.repository.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final StoreInventoryRepository storeInventoryRepository;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<SizeResponse> getSizesByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BizMapException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return productSizeRepository.findAllByProductId(productId).stream()
                .map(SizeResponse::from)
                .toList();
    }

    public List<StoreSearchResponse> searchStores(double lat, double lng, Long productId, String size, double radius) {
        double radiusMeters = radius * 1000.0;
        List<Object[]> results = storeInventoryRepository.findStoresWithInventory(lat, lng, productId, size, radiusMeters);

        return results.stream()
                .map(row -> StoreSearchResponse.builder()
                        .storeId(((Number) row[0]).longValue())
                        .storeName((String) row[1])
                        .address((String) row[2])
                        .distance(Math.round(((Number) row[3]).doubleValue() * 10) / 10.0)
                        .quantity(((Number) row[4]).intValue())
                        .latitude(((Number) row[5]).doubleValue())
                        .longitude(((Number) row[6]).doubleValue())
                        .build())
                .toList();
    }
}
