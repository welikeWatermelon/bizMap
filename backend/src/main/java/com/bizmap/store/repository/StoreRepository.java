package com.bizmap.store.repository;

import com.bizmap.store.domain.Store;
import com.bizmap.store.domain.StoreCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findAllByCompanyIdAndIsActiveTrue(Long companyId, Pageable pageable);

    Optional<Store> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true " +
            "AND s.name LIKE %:keyword%")
    Page<Store> searchByKeyword(@Param("companyId") Long companyId,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    @Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true " +
            "AND s.category = :category")
    Page<Store> findByCategory(@Param("companyId") Long companyId,
                               @Param("category") StoreCategory category,
                               Pageable pageable);

    @Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true " +
            "AND s.name LIKE %:keyword% AND s.category = :category")
    Page<Store> searchByKeywordAndCategory(@Param("companyId") Long companyId,
                                           @Param("keyword") String keyword,
                                           @Param("category") StoreCategory category,
                                           Pageable pageable);

    @Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true")
    List<Store> findAllActiveByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT s.id, s.name, s.latitude, s.longitude FROM Store s " +
            "WHERE s.companyId = :companyId AND s.isActive = true")
    List<Object[]> findMapPinsByCompanyId(@Param("companyId") Long companyId);

    @Query(value = """
            SELECT s.id, s.name, s.address,
                   (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude))
                   * cos(radians(s.longitude) - radians(:lng))
                   + sin(radians(:lat)) * sin(radians(s.latitude)))) AS distance
            FROM stores s
            WHERE s.company_id = :companyId AND s.is_active = true
            AND (6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude))
                 * cos(radians(s.longitude) - radians(:lng))
                 + sin(radians(:lat)) * sin(radians(s.latitude)))) <= :radius
            ORDER BY distance
            """, nativeQuery = true)
    List<Object[]> findNearby(@Param("companyId") Long companyId,
                              @Param("lat") double lat,
                              @Param("lng") double lng,
                              @Param("radius") double radius);

    long countByCompanyId(Long companyId);

    long countByCompanyIdAndIsActiveTrue(Long companyId);

    @Query("SELECT s.category, COUNT(s) FROM Store s " +
            "WHERE s.companyId = :companyId AND s.isActive = true GROUP BY s.category")
    List<Object[]> countByCategory(@Param("companyId") Long companyId);

    @Query("SELECT s FROM Store s WHERE s.companyId = :companyId AND s.isActive = true " +
            "ORDER BY s.createdAt DESC")
    List<Store> findRecentStores(@Param("companyId") Long companyId, Pageable pageable);
}
