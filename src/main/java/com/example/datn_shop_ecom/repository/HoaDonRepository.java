package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    List<HoaDon> findByKhachHangIdOrderByNgayTaoDesc(Long khachHangId);
    List<HoaDon> findByKhachHangEmailOrderByNgayTaoDesc(String email);
    
    boolean existsByKhachHangIdAndIdPhieuGiamGia(Long khachHangId, Long idPhieuGiamGia);

    @org.springframework.data.jpa.repository.Query("SELECT h.idPhieuGiamGia FROM HoaDon h WHERE h.khachHang.id = :khId AND h.idPhieuGiamGia IS NOT NULL")
    List<Long> findUsedVoucherIdsByKhachHangId(@org.springframework.data.repository.query.Param("khId") Long khId);
}
