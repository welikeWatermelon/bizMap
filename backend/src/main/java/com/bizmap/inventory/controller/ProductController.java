package com.bizmap.inventory.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.inventory.dto.ProductResponse;
import com.bizmap.inventory.dto.SizeResponse;
import com.bizmap.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllProducts()));
    }

    @GetMapping("/{id}/sizes")
    public ResponseEntity<ApiResponse<List<SizeResponse>>> getSizes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getSizesByProductId(id)));
    }
}
