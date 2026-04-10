package com.bizmap.store.dto;

import com.bizmap.store.domain.StoreCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class UpdateStoreRequest {

    private String name;
    private StoreCategory category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
}
