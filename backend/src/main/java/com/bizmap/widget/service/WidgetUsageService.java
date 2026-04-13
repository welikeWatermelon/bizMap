package com.bizmap.widget.service;

import com.bizmap.common.util.SecurityUtils;
import com.bizmap.widget.domain.WidgetKey;
import com.bizmap.widget.domain.WidgetUsage;
import com.bizmap.widget.dto.DailyUsageResponse;
import com.bizmap.widget.repository.WidgetKeyRepository;
import com.bizmap.widget.repository.WidgetUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetUsageService {

    private final WidgetKeyRepository widgetKeyRepository;
    private final WidgetUsageRepository widgetUsageRepository;
    private final WidgetCacheService widgetCacheService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<DailyUsageResponse> getMyUsageSummary(int days) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        List<WidgetKey> keys = widgetKeyRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId);

        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        // 날짜별 합산 맵
        Map<String, Long> dateMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            dateMap.put(startDate.plusDays(i).format(DATE_FMT), 0L);
        }

        // DB 사용량 합산
        for (WidgetKey key : keys) {
            List<WidgetUsage> usages = widgetUsageRepository.findByWidgetKeyIdAndUsageDateBetween(
                    key.getId(), startDate, today);
            for (WidgetUsage usage : usages) {
                String date = usage.getUsageDate().format(DATE_FMT);
                dateMap.merge(date, usage.getCallCount(), Long::sum);
            }
        }

        // Redis 당일 카운터 합산
        String todayStr = today.format(DATE_FMT);
        for (WidgetKey key : keys) {
            Map<String, Long> redisUsage = widgetCacheService.getUsageByApiKey(key.getApiKey(), 1);
            Long redisCount = redisUsage.getOrDefault(todayStr, 0L);
            if (redisCount > 0) {
                dateMap.merge(todayStr, redisCount, Long::sum);
            }
        }

        return dateMap.entrySet().stream()
                .map(e -> new DailyUsageResponse(e.getKey(), e.getValue()))
                .toList();
    }
}
