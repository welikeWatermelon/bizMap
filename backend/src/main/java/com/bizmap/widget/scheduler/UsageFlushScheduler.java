package com.bizmap.widget.scheduler;

import com.bizmap.widget.domain.WidgetKey;
import com.bizmap.widget.domain.WidgetUsage;
import com.bizmap.widget.repository.WidgetKeyRepository;
import com.bizmap.widget.repository.WidgetUsageRepository;
import com.bizmap.widget.service.WidgetCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageFlushScheduler {

    private final WidgetKeyRepository widgetKeyRepository;
    private final WidgetUsageRepository widgetUsageRepository;
    private final WidgetCacheService widgetCacheService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void flushUsageToDb() {
        log.info("위젯 사용량 Redis → DB flush ���작");
        List<WidgetKey> keys = widgetKeyRepository.findAll();

        for (WidgetKey key : keys) {
            Map<String, Long> usage = widgetCacheService.getUsageByApiKey(key.getApiKey(), 1);
            usage.forEach((date, count) -> {
                if (count > 0) {
                    java.time.LocalDate usageDate = java.time.LocalDate.parse(date);
                    widgetUsageRepository.findByWidgetKeyIdAndUsageDate(key.getId(), usageDate)
                            .ifPresentOrElse(
                                    existing -> existing.addCallCount(count),
                                    () -> widgetUsageRepository.save(WidgetUsage.builder()
                                            .widgetKeyId(key.getId())
                                            .usageDate(usageDate)
                                            .callCount(count)
                                            .build())
                            );
                }
            });
        }
        log.info("위젯 사용량 flush 완료: {} 키 처리", keys.size());
    }
}
