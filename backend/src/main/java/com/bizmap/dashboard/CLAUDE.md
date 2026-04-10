# Dashboard Agent

## 담당 범위
대시보드 집계 API. Store Agent 완료 후 작업.

## 엔드포인트
GET /api/dashboard/summary

## 응답 형식
{
  "totalStores": 42,
  "activeStores": 40,
  "categoryStats": [
    { "category": "RETAIL", "count": 20 },
    { "category": "FOOD", "count": 12 },
    { "category": "SERVICE", "count": 8 }
  ],
  "recentStores": [
    { "id": 1, "name": "강남점", "createdAt": "2026-04-01" }
  ]
}

## 규칙
- 모든 집계는 company_id 기준
- recentStores: 최근 등록 5개, is_active = true만
- N+1 금지, 집계는 단일 @Query로

## 생성할 파일 목록
- DashboardController.java
- DashboardService.java
- DashboardDto.java  (SummaryResponse, CategoryStat, RecentStore)

## 구현 순서
1. DashboardDto.java
2. DashboardService.java (StoreRepository 재활용)
3. DashboardController.java
