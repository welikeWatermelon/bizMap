package com.bizmap.widget.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "widget_keys")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class WidgetKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "allowed_origin")
    private String allowedOrigin;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WidgetKey(Long companyId, String name, String apiKey, String allowedOrigin) {
        this.companyId = companyId;
        this.name = name;
        this.apiKey = apiKey;
        this.allowedOrigin = allowedOrigin;
    }
}
