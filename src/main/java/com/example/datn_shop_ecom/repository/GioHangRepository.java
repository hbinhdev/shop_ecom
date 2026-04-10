package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.GioHang;
import com.example.datn_shop_ecom.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findByKhachHangId(Long khachHangId);
    Optional<GioHang> findByKhachHang(KhachHang khachHang);
}
