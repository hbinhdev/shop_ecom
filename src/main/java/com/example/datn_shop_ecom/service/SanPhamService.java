package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamService {
    Page<SanPham> filterSanPhamPage(String search, Pageable pageable);
    Page<SanPham> filterSanPham(String search, Long idThuongHieu, Long idChatLieu, Pageable pageable);
    SanPham saveSanPham(SanPham sanPham);
    SanPham findById(Long id);
    void toggleStatus(Long id);

    java.io.ByteArrayInputStream exportToExcel(String search, Long idThuongHieu, Long idChatLieu);
}
