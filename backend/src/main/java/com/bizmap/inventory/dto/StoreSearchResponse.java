package com.bizmap.inventory.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreSearchResponse {

    private Long storeId;
    private String storeName;
    private String address;
    private Double distance;
    private Integer quantity;
    private Double latitude;
    private Double longitude;
}
