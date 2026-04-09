package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {
    List<LichSuThanhToan> findByHoaDonIdOrderByNgayTaoDesc(Long hoaDonId);
}

