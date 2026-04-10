package com.bizmap.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String createAccessToken(Long companyId, String email) {
        return createToken(companyId, email, accessExpiration);
    }

    public String createRefreshToken(Long companyId, String email) {
        return createToken(companyId, email, refreshExpiration);
    }

    private String createToken(Long companyId, String email, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(companyId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getCompanyId(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
