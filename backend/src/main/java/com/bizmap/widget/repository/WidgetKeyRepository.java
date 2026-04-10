package com.bizmap.widget.repository;

import com.bizmap.widget.domain.WidgetKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WidgetKeyRepository extends JpaRepository<WidgetKey, Long> {

    List<WidgetKey> findAllByCompanyIdOrderByCreatedAtDesc(Long companyId);

    Optional<WidgetKey> findByApiKey(String apiKey);
}
