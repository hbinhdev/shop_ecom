package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface HoaDonService {
    Page<HoaDon> searchInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayTao, Pageable pageable);
    HoaDon findById(Long id);
    java.util.List<com.example.datn_shop_ecom.entity.ChiTietHoaDon> findDetailByHoaDonId(Long hoaDonId);
    java.util.List<com.example.datn_shop_ecom.entity.LichSuHoaDon> findHistoryByHoaDonId(Long hoaDonId);
    java.util.List<com.example.datn_shop_ecom.entity.LichSuThanhToan> findPaymentHistoryByHoaDonId(Long hoaDonId);
}
