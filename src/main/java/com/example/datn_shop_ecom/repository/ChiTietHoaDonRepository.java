package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long> {
    List<ChiTietHoaDon> findByHoaDonId(Long hoaDonId);
}
