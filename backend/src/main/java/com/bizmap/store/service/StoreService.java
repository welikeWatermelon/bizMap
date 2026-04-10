package com.bizmap.store.service;

import com.bizmap.common.exception.BizMapException;
import com.bizmap.common.exception.ErrorCode;
import com.bizmap.common.util.SecurityUtils;
import com.bizmap.store.domain.Store;
import com.bizmap.store.domain.StoreCategory;
import com.bizmap.store.dto.*;
import com.bizmap.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public Page<StoreResponse> getStores(String keyword, StoreCategory category, int page, int size) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Store> stores;
        if (keyword != null && category != null) {
            stores = storeRepository.searchByKeywordAndCategory(companyId, keyword, category, pageable);
        } else if (keyword != null) {
            stores = storeRepository.searchByKeyword(companyId, keyword, pageable);
        } else if (category != null) {
            stores = storeRepository.findByCategory(companyId, category, pageable);
        } else {
            stores = storeRepository.findAllByCompanyIdAndIsActiveTrue(companyId, pageable);
        }

        return stores.map(StoreResponse::from);
    }

    public StoreResponse createStore(CreateStoreRequest request) {
        Long companyId = SecurityUtils.getCurrentCompanyId();

        Store store = Store.builder()
                .companyId(companyId)
                .name(request.getName())
                .category(request.getCategory())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();

        return StoreResponse.from(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponse getStore(Long id) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Store store = findActiveStore(id);
        validateOwnership(store, companyId);
        return StoreResponse.from(store);
    }

    public StoreResponse updateStore(Long id, UpdateStoreRequest request) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Store store = findActiveStore(id);
        validateOwnership(store, companyId);

        store.update(request.getName(), request.getCategory(), request.getAddress(),
                request.getLatitude(), request.getLongitude(), request.getPhone(),
                request.getOpenTime(), request.getCloseTime());

        return StoreResponse.from(store);
    }

    public void deleteStore(Long id) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        Store store = findActiveStore(id);
        validateOwnership(store, companyId);
        store.deactivate();
    }

    @Transactional(readOnly = true)
    public List<MapPinResponse> getMapPins() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return storeRepository.findMapPinsByCompanyId(companyId).stream()
                .map(row -> MapPinResponse.builder()
                        .id((Long) row[0])
                        .name((String) row[1])
                        .latitude((Double) row[2])
                        .longitude((Double) row[3])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NearbyStoreResponse> getNearbyStores(double lat, double lng, double radius) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return storeRepository.findNearby(companyId, lat, lng, radius).stream()
                .map(row -> NearbyStoreResponse.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .address((String) row[2])
                        .distance(Math.round(((Number) row[3]).doubleValue() * 100.0) / 100.0)
                        .build())
                .toList();
    }

    private Store findActiveStore(Long id) {
        return storeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new BizMapException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateOwnership(Store store, Long companyId) {
        if (!store.getCompanyId().equals(companyId)) {
            throw new BizMapException(ErrorCode.FORBIDDEN);
        }
    }
}
