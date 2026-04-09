package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long> {
    List<LichSuHoaDon> findByHoaDonIdOrderByNgayTaoDesc(Long hoaDonId);
}

