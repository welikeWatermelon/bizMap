package com.bizmap.widget.service;

import com.bizmap.common.exception.BizMapException;
import com.bizmap.common.exception.ErrorCode;
import com.bizmap.common.util.SecurityUtils;
import com.bizmap.store.domain.Store;
import com.bizmap.store.repository.StoreRepository;
import com.bizmap.widget.domain.WidgetKey;
import com.bizmap.widget.dto.CreateWidgetKeyRequest;
import com.bizmap.widget.dto.WidgetKeyResponse;
import com.bizmap.widget.dto.WidgetStoreResponse;
import com.bizmap.widget.repository.WidgetKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WidgetKeyService {

    private final WidgetKeyRepository widgetKeyRepository;
    private final StoreRepository storeRepository;
    private final WidgetCacheService widgetCacheService;

    public WidgetKeyResponse createWidgetKey(CreateWidgetKeyRequest request) {
        Long companyId = SecurityUtils.getCurrentCompanyId();

        WidgetKey widgetKey = WidgetKey.builder()
                .companyId(companyId)
                .name(request.getName())
                .apiKey(generateApiKey())
                .allowedOrigin(request.getAllowedOrigin())
                .build();

        return WidgetKeyResponse.from(widgetKeyRepository.save(widgetKey));
    }

    @Transactional(readOnly = true)
    public List<WidgetKeyResponse> getMyWidgetKeys() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return widgetKeyRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(WidgetKeyResponse::from)
                .toList();
    }

    public void deleteWidgetKey(Long id) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        WidgetKey widgetKey = widgetKeyRepository.findById(id)
                .orElseThrow(() -> new BizMapException(ErrorCode.WIDGET_KEY_NOT_FOUND));
        if (!widgetKey.getCompanyId().equals(companyId)) {
            throw new BizMapException(ErrorCode.FORBIDDEN);
        }
        widgetKeyRepository.delete(widgetKey);
        widgetCacheService.evictWidgetKey(widgetKey.getApiKey());
        widgetCacheService.evictStores(companyId);
    }

    @Transactional(readOnly = true)
    public List<WidgetStoreResponse> getStoresByApiKey(String apiKey) {
        WidgetKey widgetKey = findByApiKey(apiKey);
        widgetCacheService.incrementUsage(apiKey);

        Optional<List<WidgetStoreResponse>> cached = widgetCacheService.getCachedStores(widgetKey.getCompanyId());
        if (cached.isPresent()) {
            return cached.get();
        }

        List<WidgetStoreResponse> stores = storeRepository.findAllActiveByCompanyId(widgetKey.getCompanyId()).stream()
                .map(WidgetStoreResponse::from)
                .toList();
        widgetCacheService.cacheStores(widgetKey.getCompanyId(), stores);
        return stores;
    }

    @Transactional(readOnly = true)
    public List<WidgetStoreResponse> getNearbyStoresByApiKey(String apiKey, double lat, double lng, double radius) {
        WidgetKey widgetKey = findByApiKey(apiKey);
        widgetCacheService.incrementUsage(apiKey);
        Long companyId = widgetKey.getCompanyId();

        List<Store> all = storeRepository.findAllActiveByCompanyId(companyId);
        return all.stream()
                .filter(s -> distanceKm(lat, lng, s.getLatitude(), s.getLongitude()) <= radius)
                .map(WidgetStoreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WidgetKey findByApiKey(String apiKey) {
        Optional<WidgetKey> cached = widgetCacheService.getCachedWidgetKey(apiKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        WidgetKey widgetKey = widgetKeyRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BizMapException(ErrorCode.WIDGET_KEY_NOT_FOUND));
        widgetCacheService.cacheWidgetKey(apiKey, widgetKey);
        return widgetKey;
    }

    private String generateApiKey() {
        return "wk_" + UUID.randomUUID().toString().replace("-", "");
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
