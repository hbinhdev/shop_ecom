package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.KhachHang;

public interface KhachHangService {
    java.util.List<KhachHang> filterKhachHang(String search, String gioiTinh, Boolean xoaMem);
    java.util.List<KhachHang> getAllKhachHangs();
    KhachHang saveKhachHang(KhachHang khachHang);
    KhachHang findById(Long id);
    void toggleStatus(Long id);
    String generateMaKhachHang();
    java.io.ByteArrayInputStream exportToExcel(String search, String gioiTinh, Boolean xoaMem);
}
