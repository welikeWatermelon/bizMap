package com.bizmap.widget.dto;

import com.bizmap.widget.domain.WidgetKey;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WidgetKeyResponse {

    private Long id;
    private String name;
    private String apiKey;
    private String allowedOrigin;
    private LocalDateTime createdAt;

    public static WidgetKeyResponse from(WidgetKey widgetKey) {
        return WidgetKeyResponse.builder()
                .id(widgetKey.getId())
                .name(widgetKey.getName())
                .apiKey(widgetKey.getApiKey())
                .allowedOrigin(widgetKey.getAllowedOrigin())
                .createdAt(widgetKey.getCreatedAt())
                .build();
    }
}
