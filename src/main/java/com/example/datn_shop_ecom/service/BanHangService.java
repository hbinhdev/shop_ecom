package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.ChiTietHoaDon;
import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.entity.PhieuGiamGia;
import com.example.datn_shop_ecom.entity.SanPhamChiTiet;

import java.math.BigDecimal;
import java.util.List;

public interface BanHangService {

    // ── Task 2: Sản phẩm ──────────────────────────────────────────
    List<SanPhamChiTiet> searchProducts(String keyword);
    SanPhamChiTiet findByBarcode(String maVach);
    ChiTietHoaDon addProduct(Long hoaDonId, Long spctId, int soLuong, String nguoiThuc);
    ChiTietHoaDon updateQuantity(Long chiTietId, int soLuongMoi, String nguoiThuc);
    void removeItem(Long chiTietId, String nguoiThuc);
    List<ChiTietHoaDon> getCartItems(Long hoaDonId);

    // ── Task 3: Khách hàng ────────────────────────────────────────
    /** Tìm kiếm khách hàng nhanh theo tên / SĐT */
    List<KhachHang> searchCustomers(String keyword);

    /** Gán khách hàng vào hóa đơn */
    HoaDon assignCustomer(Long hoaDonId, Long khachHangId, String nguoiThuc);

    /** Gỡ khách hàng khỏi hóa đơn */
    HoaDon removeCustomer(Long hoaDonId, String nguoiThuc);

    /** Tạo nhanh khách hàng tại quầy (chỉ cần tên + SĐT) */
    KhachHang quickCreateCustomer(String tenDayDu, String soDienThoai, String email, String nguoiThuc);

    /** Lấy danh sách phiếu giảm giá áp dụng được theo tổng tiền */
    List<PhieuGiamGia> getApplicableVouchers(BigDecimal tongTien);

    /** Áp dụng phiếu giảm giá vào hóa đơn */
    HoaDon applyVoucher(Long hoaDonId, Long phieuId, String nguoiThuc);

    /** Gỡ phiếu giảm giá khỏi hóa đơn */
    HoaDon removeVoucher(Long hoaDonId, String nguoiThuc);

    // ── Task 4: Thanh toán ───────────────────────────────────────
    /** Thanh toán và hoàn tất hóa đơn */
    HoaDon checkout(Long hoaDonId, java.math.BigDecimal tienKhachDua, String phuongThuc, String loaiDon, String nguoiThuc);

    /** Hủy hóa đơn chờ (hoàn trả tồn kho) */
    void cancelInvoice(Long hoaDonId, String nguoiThuc);
}