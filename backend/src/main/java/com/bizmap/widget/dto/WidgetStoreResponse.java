package com.bizmap.widget.dto;

import com.bizmap.store.domain.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WidgetStoreResponse {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;

    public static WidgetStoreResponse from(Store store) {
        return WidgetStoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .build();
    }
}
