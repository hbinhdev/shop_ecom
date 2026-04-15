package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SanPhamService {
    Page<SanPham> filterSanPhamPage(String search, Boolean trangThai, Long idDanhMuc, Long idThuongHieu, Long idKieuDang, Long idChatLieu, Pageable pageable);
    Page<SanPham> filterSanPham(String search, Boolean trangThai, Long idDanhMuc, Long idThuongHieu, Long idKieuDang, Long idChatLieu, Pageable pageable);
    SanPham saveSanPham(SanPham sanPham);
    SanPham findById(Long id);
    java.util.List<SanPham> findAll();
    java.util.List<SanPham> findAllByXoaMemFalse();
    java.util.List<SanPham> findByClientFilters(String search, Long idDanhMuc, Long idThuongHieu, Long idKieuDang, Long idChatLieu, Long idMauSac, Long idKichThuoc, String sort);
    java.util.List<SanPham> getTopBestSellers(int limit);
    java.util.List<SanPham> getLatestProducts(int limit);
    void toggleStatus(Long id);

    java.io.ByteArrayInputStream exportToExcel(String search, Boolean trangThai, Long idDanhMuc, Long idThuongHieu, Long idKieuDang, Long idChatLieu);
}

