package com.example.datn_shop_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDTO {

    // ── Kỳ được chọn ──────────────────────────────────────
    private String selectedPeriod;   // "today" | "week" | "month" | "year"
    private String periodDisplay;    // "HÔM NAY (2026-03-10)"
    private LocalDate ngayHienTai;

    // ── 4 chỉ số tóm tắt (theo kỳ đang chọn) ─────────────
    /** Tổng số đơn (mọi trạng thái, trừ DA_HUY) */
    private Long tongSoDon;
    /** Tổng doanh thu = SUM(tong_tien) của đơn HOAN_THANH (trước giảm giá) */
    private BigDecimal tongDoanhThu;
    /** Doanh thu thực tế = SUM(tong_tien_after_giam) của đơn HOAN_THANH */
    private BigDecimal doanhThuThucTe;
    /** Doanh thu dự kiến = SUM(tong_tien_after_giam) của đơn đang xử lý (chưa hoàn thành, chưa hủy) */
    private BigDecimal doanhThuDuKien;

    // ── 4 thẻ kỳ cố định (luôn hiển thị hôm nay/tuần/tháng/năm) ──
    private PeriodStatDTO today;
    private PeriodStatDTO week;
    private PeriodStatDTO month;
    private PeriodStatDTO year;

    // ── Dữ liệu biểu đồ doanh thu (kỳ đang chọn) ────────
    private String chartLabelsJson;
    private String chartDoanhThuJson;

    // ── Biểu đồ trạng thái đơn hàng (donut) ─────────────
    private String statusLabelsJson;
    private String statusDataJson;
    private String statusColorsJson;

    // ── Bảng chi tiết 4 kỳ ───────────────────────────────
    private List<ThongKeChiTietDTO> chiTiet;

    // ── Top 10 sản phẩm bán chạy (30 ngày gần nhất) ──────
    private List<TopSanPhamDTO> topSanPham;
    private LocalDate topFrom;
    private LocalDate topTo;
}