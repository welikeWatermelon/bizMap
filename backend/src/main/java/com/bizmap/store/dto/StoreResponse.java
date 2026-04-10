package com.bizmap.store.dto;

import com.bizmap.store.domain.Store;
import com.bizmap.store.domain.StoreCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class StoreResponse {

    private Long id;
    private Long companyId;
    private String name;
    private StoreCategory category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .companyId(store.getCompanyId())
                .name(store.getName())
                .category(store.getCategory())
                .address(store.getAddress())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .phone(store.getPhone())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
