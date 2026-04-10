package com.bizmap.widget.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.widget.dto.CreateWidgetKeyRequest;
import com.bizmap.widget.dto.WidgetKeyResponse;
import com.bizmap.widget.service.WidgetKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/widget-keys")
@RequiredArgsConstructor
public class WidgetKeyController {

    private final WidgetKeyService widgetKeyService;

    @PostMapping
    public ResponseEntity<ApiResponse<WidgetKeyResponse>> createWidgetKey(
            @Valid @RequestBody CreateWidgetKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(widgetKeyService.createWidgetKey(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WidgetKeyResponse>>> getMyWidgetKeys() {
        return ResponseEntity.ok(ApiResponse.success(widgetKeyService.getMyWidgetKeys()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWidgetKey(@PathVariable Long id) {
        widgetKeyService.deleteWidgetKey(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
