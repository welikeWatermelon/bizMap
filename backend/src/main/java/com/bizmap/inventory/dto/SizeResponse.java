package com.bizmap.inventory.dto;

import com.bizmap.inventory.domain.ProductSize;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SizeResponse {

    private Long id;
    private String size;

    public static SizeResponse from(ProductSize productSize) {
        return SizeResponse.builder()
                .id(productSize.getId())
                .size(productSize.getSize().name())
                .build();
    }
}
