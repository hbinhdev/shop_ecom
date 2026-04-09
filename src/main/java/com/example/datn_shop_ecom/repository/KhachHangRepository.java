package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    @Query("SELECT k FROM KhachHang k " + 
           "WHERE (:search IS NULL OR k.maKhachHang LIKE %:search% " +
           "OR k.tenDayDu LIKE %:search% " +
           "OR k.email LIKE %:search% OR k.soDienThoai LIKE %:search%) " +
           "AND (:gioiTinh IS NULL OR k.gioiTinh = :gioiTinh) " +
           "AND (:xoaMem IS NULL OR k.xoaMem = :xoaMem)")
    org.springframework.data.domain.Page<KhachHang> findByFiltersPage(@Param("search") String search, 
                                                                    @Param("gioiTinh") String gioiTinh, 
                                                                    @Param("xoaMem") Boolean xoaMem,
                                                                    org.springframework.data.domain.Pageable pageable);

    @Query("SELECT k FROM KhachHang k LEFT JOIN FETCH k.danhSachDiaChi " +
           "WHERE (:search IS NULL OR k.maKhachHang LIKE %:search% " +
           "OR k.tenDayDu LIKE %:search% " +
           "OR k.email LIKE %:search% OR k.soDienThoai LIKE %:search%) " +
           "AND (:gioiTinh IS NULL OR k.gioiTinh = :gioiTinh) " +
           "AND (:xoaMem IS NULL OR k.xoaMem = :xoaMem)")
    java.util.List<KhachHang> findByFilters(@org.springframework.data.repository.query.Param("search") String search, 
                                            @org.springframework.data.repository.query.Param("gioiTinh") String gioiTinh, 
                                            @org.springframework.data.repository.query.Param("xoaMem") Boolean xoaMem);

    java.util.List<KhachHang> findAllByXoaMemFalse();
    Optional<KhachHang> findByMaKhachHang(String maKhachHang);
    Optional<KhachHang> findByEmail(String email);
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE KhachHang k SET k.xoaMem = :xoaMem, k.ngaySuaCuoi = CURRENT_TIMESTAMP, k.nguoiSuaCuoi = 'Admin' WHERE k.id = :id")
    void updateStatus(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("xoaMem") Boolean xoaMem);
}

