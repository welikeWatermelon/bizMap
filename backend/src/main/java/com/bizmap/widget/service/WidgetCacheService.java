package com.bizmap.widget.service;

import com.bizmap.store.dto.MapPinResponse;
import com.bizmap.widget.domain.WidgetKey;
import com.bizmap.widget.dto.WidgetStoreResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long WIDGET_KEY_TTL_MINUTES = 10;
    private static final long STORES_TTL_MINUTES = 5;
    private static final long USAGE_TTL_HOURS = 25;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---- WidgetKey 캐싱 ----

    public Optional<WidgetKey> getCachedWidgetKey(String apiKey) {
        try {
            String json = redisTemplate.opsForValue().get("widget:key:" + apiKey);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, WidgetKey.class));
        } catch (Exception e) {
            log.warn("Redis getCachedWidgetKey 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void cacheWidgetKey(String apiKey, WidgetKey widgetKey) {
        try {
            String json = objectMapper.writeValueAsString(widgetKey);
            redisTemplate.opsForValue().set("widget:key:" + apiKey, json, WIDGET_KEY_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cacheWidgetKey 실패: {}", e.getMessage());
        }
    }

    public void evictWidgetKey(String apiKey) {
        try {
            redisTemplate.delete("widget:key:" + apiKey);
        } catch (Exception e) {
            log.warn("Redis evictWidgetKey 실패: {}", e.getMessage());
        }
    }

    // ---- 매장 목록 캐싱 ----

    public Optional<List<WidgetStoreResponse>> getCachedStores(Long companyId) {
        try {
            String json = redisTemplate.opsForValue().get("widget:stores:" + companyId);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<List<WidgetStoreResponse>>() {}));
        } catch (Exception e) {
            log.warn("Redis getCachedStores 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void cacheStores(Long companyId, List<WidgetStoreResponse> stores) {
        try {
            String json = objectMapper.writeValueAsString(stores);
            redisTemplate.opsForValue().set("widget:stores:" + companyId, json, STORES_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cacheStores 실패: {}", e.getMessage());
        }
    }

    public void evictStores(Long companyId) {
        try {
            redisTemplate.delete("widget:stores:" + companyId);
        } catch (Exception e) {
            log.warn("Redis evictStores 실패: {}", e.getMessage());
        }
    }

    // ---- 지도 핀 캐싱 ----

    public Optional<List<MapPinResponse>> getCachedMapPins(Long companyId) {
        try {
            String json = redisTemplate.opsForValue().get("map:pins:" + companyId);
            if (json == null) return Optional.empty();
            return Optional.of(objectMapper.readValue(json, new TypeReference<List<MapPinResponse>>() {}));
        } catch (Exception e) {
            log.warn("Redis getCachedMapPins 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void cacheMapPins(Long companyId, List<MapPinResponse> pins) {
        try {
            String json = objectMapper.writeValueAsString(pins);
            redisTemplate.opsForValue().set("map:pins:" + companyId, json, STORES_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis cacheMapPins 실패: {}", e.getMessage());
        }
    }

    public void evictMapPins(Long companyId) {
        try {
            redisTemplate.delete("map:pins:" + companyId);
        } catch (Exception e) {
            log.warn("Redis evictMapPins 실패: {}", e.getMessage());
        }
    }

    // ---- 사용량 카운팅 ----

    public void incrementUsage(String apiKey) {
        try {
            String key = "usage:" + apiKey + ":" + LocalDate.now().format(DATE_FMT);
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, USAGE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis incrementUsage 실패: {}", e.getMessage());
        }
    }

    public Map<String, Long> getUsageByApiKey(String apiKey, int days) {
        Map<String, Long> result = new LinkedHashMap<>();
        try {
            LocalDate today = LocalDate.now();
            for (int i = days - 1; i >= 0; i--) {
                String date = today.minusDays(i).format(DATE_FMT);
                String key = "usage:" + apiKey + ":" + date;
                String val = redisTemplate.opsForValue().get(key);
                result.put(date, val != null ? Long.parseLong(val) : 0L);
            }
        } catch (Exception e) {
            log.warn("Redis getUsageByApiKey 실패: {}", e.getMessage());
        }
        return result;
    }
}
