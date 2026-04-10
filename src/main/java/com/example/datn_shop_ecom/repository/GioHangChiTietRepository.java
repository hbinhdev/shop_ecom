package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Long> {
    List<GioHangChiTiet> findByGioHangId(Long gioHangId);
    Optional<GioHangChiTiet> findByGioHangIdAndSanPhamChiTietId(Long gioHangId, Long spctId);
    void deleteByGioHangId(Long gioHangId);
}
