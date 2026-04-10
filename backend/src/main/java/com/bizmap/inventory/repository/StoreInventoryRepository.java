package com.bizmap.inventory.repository;

import com.bizmap.inventory.domain.StoreInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    @Query(value = """
            SELECT s.id AS store_id, s.name AS store_name, s.address,
                   ST_Distance(
                       CAST(ST_MakePoint(s.longitude, s.latitude) AS geography),
                       CAST(ST_MakePoint(:lng, :lat) AS geography)
                   ) / 1000.0 AS distance,
                   si.quantity,
                   s.latitude, s.longitude
            FROM store_inventory si
            JOIN stores s ON s.id = si.store_id
            WHERE si.product_id = :productId
              AND si.size = :size
              AND si.quantity > 0
              AND s.is_active = true
              AND ST_DWithin(
                  CAST(ST_MakePoint(s.longitude, s.latitude) AS geography),
                  CAST(ST_MakePoint(:lng, :lat) AS geography),
                  :radiusMeters
              )
            ORDER BY distance
            """, nativeQuery = true)
    List<Object[]> findStoresWithInventory(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("productId") Long productId,
            @Param("size") String size,
            @Param("radiusMeters") double radiusMeters);
}
