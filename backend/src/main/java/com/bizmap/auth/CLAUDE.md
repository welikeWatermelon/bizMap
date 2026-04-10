# Auth Agent

## 담당 범위
Company 회원가입/로그인, JWT 발급·갱신, Spring Security 설정.
Common Agent 구현 완료 후 작업 시작.

## 엔드포인트
POST /api/auth/register   → 회사 회원가입
POST /api/auth/login      → 로그인, JWT 발급
POST /api/auth/refresh    → Access Token 갱신

## JWT 규칙
- Access Token 만료: 30분 (application.yml jwt.access-expiration)
- Refresh Token 만료: 7일 (application.yml jwt.refresh-expiration)
- Payload: companyId(Long), email(String)
- Refresh Token은 refresh_tokens 테이블에 저장

## 생성할 파일 목록
### domain/
- Company.java          Entity (@Table name=companies)
- RefreshToken.java     Entity (@Table name=refresh_tokens)

### repository/
- CompanyRepository.java    findByEmail(String email)
- RefreshTokenRepository.java  findByToken(String), deleteByCompanyId(Long)

### dto/
- RegisterRequest.java  { name, email, password } @Valid 포함
- LoginRequest.java     { email, password }
- TokenResponse.java    { accessToken, refreshToken }

### service/
- AuthService.java
  - register(): 중복 email 체크 → password BCrypt 암호화 → 저장
  - login(): email 조회 → password 검증 → JWT 발급 → RefreshToken 저장
  - refresh(): RefreshToken 검증 → 새 AccessToken 발급

### security/
- JwtProvider.java       토큰 생성/검증/파싱
- JwtAuthFilter.java     OncePerRequestFilter, 토큰 추출 → 인증 객체 설정
- SecurityConfig.java    필터 체인, BCryptPasswordEncoder Bean
- CustomUserDetails.java companyId, email 보유

### controller/
- AuthController.java    위 3개 엔드포인트 구현

## 구현 순서
1. Company.java, RefreshToken.java
2. Repository 2개
3. JwtProvider.java
4. CustomUserDetails.java
5. SecurityConfig.java (기본 구조만)
6. AuthService.java
7. JwtAuthFilter.java
8. SecurityConfig.java (필터 등록)
9. AuthController.java
