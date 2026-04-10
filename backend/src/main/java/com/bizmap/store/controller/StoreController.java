package com.bizmap.store.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.store.domain.StoreCategory;
import com.bizmap.store.dto.*;
import com.bizmap.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<StoreResponse>>> getStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StoreCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStores(keyword, category, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@Valid @RequestBody CreateStoreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(storeService.createStore(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStore(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateStoreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(storeService.updateStore(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/map")
    public ResponseEntity<ApiResponse<List<MapPinResponse>>> getMapPins() {
        return ResponseEntity.ok(ApiResponse.success(storeService.getMapPins()));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyStoreResponse>>> getNearbyStores(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getNearbyStores(lat, lng, radius)));
    }
}
