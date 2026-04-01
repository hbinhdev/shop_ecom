package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.ChiTietHoaDon;
import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.entity.LichSuHoaDon;
import com.example.datn_shop_ecom.entity.LichSuThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface HoaDonService {
    Page<HoaDon> searchInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc, Pageable pageable);
    List<HoaDon> findAllMatchingInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc);
    HoaDon findById(Long id);
    java.util.List<ChiTietHoaDon> findDetailByHoaDonId(Long hoaDonId);
    java.util.List<LichSuHoaDon> findHistoryByHoaDonId(Long hoaDonId);
    java.util.List<LichSuThanhToan> findPaymentHistoryByHoaDonId(Long hoaDonId);
}
