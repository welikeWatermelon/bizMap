package com.bizmap.auth.service;

import com.bizmap.auth.domain.Company;
import com.bizmap.auth.domain.RefreshToken;
import com.bizmap.auth.dto.LoginRequest;
import com.bizmap.auth.dto.RegisterRequest;
import com.bizmap.auth.dto.TokenResponse;
import com.bizmap.auth.repository.CompanyRepository;
import com.bizmap.auth.repository.RefreshTokenRepository;
import com.bizmap.auth.security.JwtProvider;
import com.bizmap.common.exception.BizMapException;
import com.bizmap.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final CompanyRepository companyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new BizMapException(ErrorCode.COMPANY_ALREADY_EXISTS);
        }

        Company company = Company.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        companyRepository.save(company);
    }

    public TokenResponse login(LoginRequest request) {
        Company company = companyRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BizMapException(ErrorCode.COMPANY_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), company.getPassword())) {
            throw new BizMapException(ErrorCode.INVALID_TOKEN, "비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(company.getId(), company.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(company.getId(), company.getEmail());

        refreshTokenRepository.deleteByCompanyId(company.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .companyId(company.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProvider.getRefreshExpiration() / 1000))
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse refresh(String refreshTokenStr) {
        if (!jwtProvider.validateToken(refreshTokenStr)) {
            throw new BizMapException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BizMapException(ErrorCode.INVALID_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BizMapException(ErrorCode.INVALID_TOKEN, "리프레시 토큰이 만료되었습니다.");
        }

        Long companyId = jwtProvider.getCompanyId(refreshTokenStr);
        String email = jwtProvider.getEmail(refreshTokenStr);
        String newAccessToken = jwtProvider.createAccessToken(companyId, email);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenStr)
                .build();
    }
}
