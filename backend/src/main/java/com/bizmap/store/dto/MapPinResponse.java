package com.bizmap.store.dto;

import com.bizmap.store.domain.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapPinResponse {

    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;

    public static MapPinResponse from(Store store) {
        return MapPinResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .build();
    }
}
