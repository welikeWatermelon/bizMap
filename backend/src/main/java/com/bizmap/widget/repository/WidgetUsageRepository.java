package com.bizmap.widget.repository;

import com.bizmap.widget.domain.WidgetUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WidgetUsageRepository extends JpaRepository<WidgetUsage, Long> {

    List<WidgetUsage> findByWidgetKeyIdAndUsageDateBetween(Long widgetKeyId, LocalDate start, LocalDate end);

    Optional<WidgetUsage> findByWidgetKeyIdAndUsageDate(Long widgetKeyId, LocalDate date);
}
