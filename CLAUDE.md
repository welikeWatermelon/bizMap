# BizMap - 매장 관리 + 지도 시각화 서비스

## 엔티티
Company { id, name, email, password, created_at }
Store {
  id, company_id(FK), name,
  category(ENUM: RETAIL/FOOD/SERVICE/OTHER),
  address, latitude, longitude,
  phone, open_time, close_time,
  is_active, created_at, updated_at
}
RefreshToken { id, company_id(FK), token, expires_at }

## API 규칙
- Base URL: /api
- 인증: Authorization: Bearer {accessToken}
- 에러 응답: { "code": "ERROR_CODE", "message": "설명" }
- 성공 응답: { "data": T, "message": "success" }

## 에러 코드 목록
COMPANY_NOT_FOUND      / 404
COMPANY_ALREADY_EXISTS / 409
STORE_NOT_FOUND        / 404
FORBIDDEN              / 403
INVALID_TOKEN          / 401
INVALID_INPUT          / 400

## Agent 구성
- backend/CLAUDE.md       백엔드 전체 총괄
- frontend/CLAUDE.md      프론트엔드 전체 총괄

## 작업 원칙
1. 새 기능 시작 전 이 파일의 비즈니스 규칙 확인
2. 해당 도메인 CLAUDE.md로 이동하여 작업
3. 작업 완료 후 반드시 PROGRESS.md 업데이트
