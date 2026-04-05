package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface SanPhamChiTietService {
    Page<SanPhamChiTiet> filterVariantPage(
        String search, Long idMauSac, Long idKichThuoc, 
        Long idLoaiSan, BigDecimal minPrice, BigDecimal maxPrice, 
        String trangThai, Pageable pageable
    );
    void toggleVariantStatus(Long id);
    SanPhamChiTiet findById(Long id);
    java.io.ByteArrayInputStream exportToExcel(String search, Long idMauSac, Long idKichThuoc, Long idLoaiSan, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String trangThai);
}
