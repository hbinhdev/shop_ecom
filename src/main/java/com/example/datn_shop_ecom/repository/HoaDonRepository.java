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

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {

    // ── POS ─────────────────────────────────────────────────────────────────────
    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiHoaDon = 'CHO_THANH_TOAN' AND h.loaiHoaDon = 'TAI_QUAY' ORDER BY h.ngayTao ASC")
    List<HoaDon> findAllPendingPOS();

    @Query("SELECT COALESCE(MAX(h.id), 0) FROM HoaDon h")
    long findMaxId();

    boolean existsByMaHoaDon(String maHoaDon);

    // ── Thống kê tổng hợp ────────────────────────────────────────────────────────

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

    /** Doanh thu thực tế (sau giảm giá) của đơn HOAN_THANH.
     *  Dùng COALESCE(tong_tien_after_giam, tong_tien) để tính đúng cả đơn cũ chưa có trường after_giam */
    @Query(value = "SELECT COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    BigDecimal sumDoanhThu(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);

    /** Doanh thu dự kiến: đơn đang xử lý (chưa hoàn thành, chưa hủy) */
    @Query(value = "SELECT COALESCE(SUM(COALESCE(h.tong_tien_after_giam, h.tong_tien)), 0) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don IN " +
                   "('CHO_THANH_TOAN','CHO_XAC_NHAN','DA_XAC_NHAN','DANG_GIAO') " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    BigDecimal sumDoanhThuDuKien(@Param("fromDate") LocalDateTime fromDate,
                                 @Param("toDate") LocalDateTime toDate);

    /** Số đơn HOAN_THANH trong kỳ */
    @Query(value = "SELECT COUNT(*) FROM hoa_don h " +
                   "WHERE h.trang_thai_hoa_don = 'HOAN_THANH' " +
                   "AND h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate", nativeQuery = true)
    Long countDonHoanThanh(@Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate);

    // ── Biểu đồ trạng thái đơn hàng (donut) ─────────────────────────────────────

    /** Đếm số đơn theo từng trạng thái trong kỳ */
    @Query(value = "SELECT h.trang_thai_hoa_don, COUNT(*) AS so_don " +
                   "FROM hoa_don h " +
                   "WHERE h.ngay_tao >= :fromDate AND h.ngay_tao < :toDate " +
                   "GROUP BY h.trang_thai_hoa_don", nativeQuery = true)
    List<Object[]> countByTrangThai(@Param("fromDate") LocalDateTime fromDate,
                                    @Param("toDate") LocalDateTime toDate);

    // ── Biểu đồ doanh thu theo thời gian ─────────────────────────────────────────

    /** Doanh thu theo giờ trong ngày (dùng cho kỳ "Hôm nay") */
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

    /** Doanh thu theo ngày (dùng cho kỳ "Tuần này" / "Tháng này") */
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

    /** Doanh thu theo tháng (dùng cho kỳ "Năm này") */
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