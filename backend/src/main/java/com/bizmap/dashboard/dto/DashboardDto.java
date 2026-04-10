package com.bizmap.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDto {

    @Getter
    @Builder
    public static class SummaryResponse {
        private long totalStores;
        private long activeStores;
        private List<CategoryStat> categoryStats;
        private List<RecentStore> recentStores;
    }

    @Getter
    @Builder
    public static class CategoryStat {
        private String category;
        private long count;
    }

    @Getter
    @Builder
    public static class RecentStore {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
    }
}
