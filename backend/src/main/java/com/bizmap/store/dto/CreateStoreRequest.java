package com.bizmap.store.dto;

import com.bizmap.store.domain.StoreCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class CreateStoreRequest {

    @NotBlank(message = "매장명은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리는 필수입니다.")
    private StoreCategory category;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    private String phone;
    private LocalTime openTime;
    private LocalTime closeTime;
}
