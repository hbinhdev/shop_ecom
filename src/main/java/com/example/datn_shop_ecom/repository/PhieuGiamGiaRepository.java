package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Long> {

    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
           "(:search IS NULL OR p.maPhieu LIKE %:search% OR p.tenPhieu LIKE %:search%) AND " +
           "(:startDate IS NULL OR p.ngayBatDau >= :startDate) AND " +
           "(:endDate IS NULL OR p.ngayKetThuc <= :endDate) AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(p.xoaMem = false)")
    org.springframework.data.domain.Page<PhieuGiamGia> findByFiltersPage(
            @Param("search") String search,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("trangThai") Integer trangThai,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
           "(:search IS NULL OR p.maPhieu LIKE %:search% OR p.tenPhieu LIKE %:search%) AND " +
           "(:startDate IS NULL OR p.ngayBatDau >= :startDate) AND " +
           "(:endDate IS NULL OR p.ngayKetThuc <= :endDate) AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(p.xoaMem = false) " +
           "ORDER BY p.ngayTao DESC")
    List<PhieuGiamGia> findByFilters(
            @Param("search") String search,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("trangThai") Integer trangThai
    );

    @Modifying
    @Transactional
    @Query("UPDATE PhieuGiamGia p SET p.trangThai = :status, p.ngaySuaCuoi = :now, p.nguoiSuaCuoi = :admin WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("now") LocalDateTime now, @Param("admin") String admin);

    @Modifying
    @Transactional
    @Query("UPDATE PhieuGiamGia p SET p.xoaMem = true, p.ngaySuaCuoi = :now WHERE p.id = :id")
    void softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);

    List<PhieuGiamGia> findAllByXoaMemFalse();
}

