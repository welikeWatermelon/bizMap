package com.bizmap.inventory.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.inventory.dto.StoreSearchResponse;
import com.bizmap.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreSearchController {

    private final InventoryService inventoryService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StoreSearchResponse>>> searchStores(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam Long productId,
            @RequestParam String size,
            @RequestParam(defaultValue = "10") double radius) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.searchStores(lat, lng, productId, size, radius)));
    }
}
