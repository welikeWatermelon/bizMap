package com.bizmap.auth.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(Long companyId, String token, LocalDateTime expiresAt) {
        this.companyId = companyId;
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
