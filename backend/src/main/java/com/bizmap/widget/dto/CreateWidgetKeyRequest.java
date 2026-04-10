package com.bizmap.widget.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateWidgetKeyRequest {

    @NotBlank
    private String name;

    private String allowedOrigin;
}
