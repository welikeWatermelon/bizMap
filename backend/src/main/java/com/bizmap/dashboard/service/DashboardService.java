package com.bizmap.dashboard.service;

import com.bizmap.common.util.SecurityUtils;
import com.bizmap.dashboard.dto.DashboardDto;
import com.bizmap.store.domain.Store;
import com.bizmap.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final StoreRepository storeRepository;

    public DashboardDto.SummaryResponse getSummary() {
        Long companyId = SecurityUtils.getCurrentCompanyId();

        long totalStores = storeRepository.countByCompanyId(companyId);
        long activeStores = storeRepository.countByCompanyIdAndIsActiveTrue(companyId);

        List<DashboardDto.CategoryStat> categoryStats = storeRepository.countByCategory(companyId).stream()
                .map(row -> DashboardDto.CategoryStat.builder()
                        .category(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .toList();

        List<DashboardDto.RecentStore> recentStores = storeRepository
                .findRecentStores(companyId, PageRequest.of(0, 5)).stream()
                .map(store -> DashboardDto.RecentStore.builder()
                        .id(store.getId())
                        .name(store.getName())
                        .createdAt(store.getCreatedAt())
                        .build())
                .toList();

        return DashboardDto.SummaryResponse.builder()
                .totalStores(totalStores)
                .activeStores(activeStores)
                .categoryStats(categoryStats)
                .recentStores(recentStores)
                .build();
    }
}
