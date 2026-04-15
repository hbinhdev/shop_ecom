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
    List<ChiTietHoaDon> findDetailByHoaDonId(Long hoaDonId);
    List<LichSuHoaDon> findHistoryByHoaDonId(Long hoaDonId);
    List<LichSuThanhToan> findPaymentHistoryByHoaDonId(Long hoaDonId);

    // POS - Bán hàng tại quầy
    HoaDon createPendingInvoice(String nhanVienEmail);
    List<HoaDon> findAllPendingPOS();
    // Client-side
    HoaDon createHoaDonOnline(HoaDon hoaDon, List<ChiTietHoaDon> items);
    HoaDon findByMaHoaDon(String maHoaDon);
    List<HoaDon> findByKhachHangEmail(String email);
    String generateMaHoaDon();
    HoaDon updateTrangThai(Long id, String status, String note, String user);
}
