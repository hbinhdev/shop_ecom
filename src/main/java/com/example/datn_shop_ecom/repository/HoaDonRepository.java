package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {

    // ── 1. TRA CỨU CƠ BẢN & CLIENT-SIDE (Gộp từ đoạn 2) ────────────────────────
    
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    
    List<HoaDon> findByKhachHangIdOrderByNgayTaoDesc(Long khachHangId);
    
    List<HoaDon> findByKhachHangEmailOrderByNgayTaoDesc(String email);
    
    boolean existsByKhachHangIdAndIdPhieuGiamGia(Long khachHangId, Long idPhieuGiamGia);

    @Query("SELECT h.idPhieuGiamGia FROM HoaDon h WHERE h.khachHang.id = :khId AND h.idPhieuGiamGia IS NOT NULL")
    List<Long> findUsedVoucherIdsByKhachHangId(@Param("khId") Long khId);

    // ── 2. POS & QUẢN LÝ (Gộp từ đoạn 1) ───────────────────────────────────────

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiHoaDon = 'CHO_THANH_TOAN' AND h.loaiHoaDon = 'TAI_QUAY' ORDER BY h.ngayTao ASC")
    List<HoaDon> findAllPendingPOS();

    @Query("SELECT COALESCE(MAX(h.id), 0) FROM HoaDon h")
    long findMaxId();

    boolean existsByMaHoaDon(String maHoaDon);

    // ── 3. THỐNG KÊ TỔNG HỢP (Native Query cho Dashboard) ──────────────────────

    /** Tổng số đơn mọi trạng thái (trừ DA_HUY) trong kỳ */
    @Query(value = "SELECT COUNT(*) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don NOT IN ('DA_HUY') " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    Long countAllDon(@Param("fromDate") LocalDateTime fromDate,
                     @Param("toDate") LocalDateTime toDate);

    /** Tổng doanh thu (trước giảm giá) của đơn HOAN_THANH */
    @Query(value = "SELECT COALESCE(SUM(h.tong_tien), 0) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    BigDecimal sumTongTien(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);

    /** Doanh thu thực tế (sau giảm giá) */
    @Query(value = "SELECT COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    BigDecimal sumDoanhThu(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);

    /** Doanh thu dự kiến (đơn đang xử lý) */
    @Query(value = "SELECT COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don IN " +
                   "('CHO_THANH_TOAN','CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GIAO') " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    BigDecimal sumDoanhThuDuKien(@Param("fromDate") LocalDateTime fromDate,
                                 @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT COUNT(*) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    Long countDonHoanThanh(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);

    // ── 4. DỮ LIỆU BIỂU ĐỒ (Charts) ─────────────────────────────────────────────

    @Query(value = "SELECT h.trang_thai_hoa_don, COUNT(*) AS so_don " +
                   "FROM hoa_don h " +
                   "WHERE h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate " +
                   "GROUP BY h.trang_thai_hoa_don", nativeQuery = true)
    List<Object[]> countByTrangThai(@Param("fromDate") LocalDateTime fromDate,
                                    @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT CAST(DATEPART(hour, h.ngay_tao) AS VARCHAR) + ':00' AS gio, " +
                   "COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) AS doanhThu, " +
                   "COUNT(*) AS soDon " +
                   "FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate " +
                   "GROUP BY DATEPART(hour, h.ngay_tao) " +
                   "ORDER BY DATEPART(hour, h.ngay_tao)", nativeQuery = true)
    List<Object[]> findDoanhThuTheoGio(@Param("fromDate") LocalDateTime fromDate,
                                       @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT CONVERT(VARCHAR(10), h.ngay_tao, 120) AS ngay, " +
                   "COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) AS doanhThu, " +
                   "COUNT(*) AS soDon " +
                   "FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate " +
                   "GROUP BY CONVERT(VARCHAR(10), h.ngay_tao, 120) " +
                   "ORDER BY ngay", nativeQuery = true)
    List<Object[]> findDoanhThuTheoNgay(@Param("fromDate") LocalDateTime fromDate,
                                        @Param("toDate") LocalDateTime toDate);

    @Query(value = "SELECT LEFT(CONVERT(VARCHAR(10), h.ngay_tao, 120), 7) AS thang, " +
                   "COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) AS doanhThu, " +
                   "COUNT(*) AS soDon " +
                   "FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate " +
                   "GROUP BY LEFT(CONVERT(VARCHAR(10), h.ngay_tao, 120), 7) " +
                   "ORDER BY thang", nativeQuery = true)
    List<Object[]> findDoanhThuTheoThang(@Param("fromDate") LocalDateTime fromDate,
                                         @Param("toDate") LocalDateTime toDate);
}