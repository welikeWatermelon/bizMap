package com.bizmap.dashboard.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.dashboard.dto.DashboardDto;
import com.bizmap.dashboard.service.DashboardService;
import com.bizmap.widget.dto.DailyUsageResponse;
import com.bizmap.widget.service.WidgetUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final WidgetUsageService widgetUsageService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDto.SummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary()));
    }

    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<List<DailyUsageResponse>>> getUsage(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(widgetUsageService.getMyUsageSummary(days)));
    }
}
