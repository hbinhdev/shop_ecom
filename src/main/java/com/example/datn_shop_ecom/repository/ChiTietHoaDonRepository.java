package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long> {
    List<ChiTietHoaDon> findByHoaDonId(Long hoaDonId);

    // POS: kiểm tra sản phẩm đã có trong hóa đơn chưa
    Optional<ChiTietHoaDon> findByHoaDonIdAndSanPhamChiTietId(Long hoaDonId, Long sanPhamChiTietId);

    // POS: tính tổng tiền hóa đơn
    @Query("SELECT COALESCE(SUM(c.thanhTien), 0) FROM ChiTietHoaDon c WHERE c.hoaDon.id = :hoaDonId")
    BigDecimal sumThanhTienByHoaDonId(@Param("hoaDonId") Long hoaDonId);

    // Thống kê: top 10 biến thể sản phẩm bán chạy
    @Query(value = "SELECT TOP 10 spct.id AS sanPhamChiTietId, " +
                   "sp.ten_san_pham AS tenSanPham, " +
                   "ms.ten_mau_sac AS tenMauSac, " +
                   "kt.ten_kich_thuoc AS tenKichThuoc, " +
                   "spct.ma_san_pham_chi_tiet AS maSanPhamChiTiet, " +
                   "spct.duong_dan_anh AS duongDanAnh, " +
                   "SUM(ct.so_luong) AS soLuongBan, " +
                   "SUM(ct.thanh_tien) AS doanhThu " +
                   "FROM chi_tiet_hoa_don ct " +
                   "JOIN hoa_don hd ON ct.id_hoa_don = hd.id " +
                   "JOIN san_pham_chi_tiet spct ON ct.id_san_pham_chi_tiet = spct.id " +
                   "JOIN san_pham sp ON spct.id_san_pham = sp.id " +
                   "JOIN mau_sac ms ON spct.id_mau_sac = ms.id " +
                   "JOIN kich_thuoc kt ON spct.id_kich_thuoc = kt.id " +
                   "WHERE hd.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND hd.ngay_tao >= :fromDate AND hd.ngay_tao < :toDate " +
                   "GROUP BY spct.id, sp.ten_san_pham, ms.ten_mau_sac, kt.ten_kich_thuoc, " +
                   "spct.ma_san_pham_chi_tiet, spct.duong_dan_anh " +
                   "ORDER BY SUM(ct.so_luong) DESC", nativeQuery = true)
    List<Object[]> findTopSanPhamBanChay(@Param("fromDate") LocalDateTime fromDate,
                                          @Param("toDate") LocalDateTime toDate);

    // Thống kê: tổng số lượng sản phẩm đã bán trong khoảng thời gian
    @Query(value = "SELECT COALESCE(SUM(ct.so_luong), 0) " +
                   "FROM chi_tiet_hoa_don ct " +
                   "JOIN hoa_don hd ON ct.id_hoa_don = hd.id " +
                   "WHERE hd.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND hd.ngay_tao >= :fromDate AND hd.ngay_tao < :toDate", nativeQuery = true)
    Long sumSoLuongBan(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}

