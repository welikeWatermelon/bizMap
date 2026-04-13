package com.bizmap.widget.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "widget_usage", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"widget_key_id", "usage_date"})
})
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class WidgetUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "widget_key_id", nullable = false)
    private Long widgetKeyId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "call_count", nullable = false)
    private Long callCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public WidgetUsage(Long widgetKeyId, LocalDate usageDate, Long callCount) {
        this.widgetKeyId = widgetKeyId;
        this.usageDate = usageDate;
        this.callCount = callCount != null ? callCount : 0L;
    }

    public void addCallCount(Long count) {
        this.callCount += count;
    }
}
