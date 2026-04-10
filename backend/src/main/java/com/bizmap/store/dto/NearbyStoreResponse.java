package com.bizmap.store.dto;

import com.bizmap.store.domain.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearbyStoreResponse {

    private Long id;
    private String name;
    private String address;
    private Double distance;

    public static NearbyStoreResponse from(Store store, double lat, double lng) {
        double distance = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
        return NearbyStoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .distance(Math.round(distance * 100.0) / 100.0)
                .build();
    }

    private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
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
