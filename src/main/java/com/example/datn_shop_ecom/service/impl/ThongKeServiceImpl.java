package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.dto.*;
import com.example.datn_shop_ecom.repository.ChiTietHoaDonRepository;
import com.example.datn_shop_ecom.repository.HoaDonRepository;
import com.example.datn_shop_ecom.service.ThongKeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ThongKeServiceImpl implements ThongKeService {

    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final ObjectMapper objectMapper;

    // Ánh xạ trạng thái DB → tên hiển thị + màu sắc
    private static final Map<String, String> STATUS_LABEL = Map.of(
            "CHO_THANH_TOAN", "Chờ thanh toán",
            "CHO_XAC_NHAN",   "Chờ xác nhận",
            "DA_XAC_NHAN",    "Chờ giao hàng",
            "DANG_GIAO",      "Đang vận chuyển",
            "HOAN_THANH",     "Hoàn thành",
            "DA_HUY",         "Đã hủy"
    );
    private static final Map<String, String> STATUS_COLOR = Map.of(
            "CHO_THANH_TOAN", "#fbc02d",
            "CHO_XAC_NHAN",   "#ef6c00",
            "DA_XAC_NHAN",    "#1976d2",
            "DANG_GIAO",      "#4e342e",
            "HOAN_THANH",     "#2e7d32",
            "DA_HUY",         "#9e9e9e"
    );
    // Thứ tự hiển thị trong donut
    private static final List<String> STATUS_ORDER = List.of(
            "CHO_THANH_TOAN", "CHO_XAC_NHAN", "DA_XAC_NHAN",
            "DANG_GIAO", "HOAN_THANH", "DA_HUY"
    );

    @Override
    public ThongKeDTO getThongKe(String period) {
        if (period == null || period.isBlank()) period = "today";

        LocalDate today = LocalDate.now();

        // ── Xác định khoảng thời gian cho kỳ đang chọn ──────────────────────────
        LocalDate[] range = periodRange(period, today);
        LocalDate from = range[0];
        LocalDate to   = range[1]; // inclusive

        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt   = to.plusDays(1).atStartOfDay(); // exclusive upper-bound

        // ── 4 chỉ số tóm tắt ────────────────────────────────────────────────────
        Long    tongSoDon      = nvl(hoaDonRepository.countAllDon(fromDt, toDt));
        BigDecimal tongDoanhThu   = nvl(hoaDonRepository.sumTongTien(fromDt, toDt));
        BigDecimal doanhThuThucTe = nvl(hoaDonRepository.sumDoanhThu(fromDt, toDt));
        BigDecimal doanhThuDuKien = nvl(hoaDonRepository.sumDoanhThuDuKien(fromDt, toDt));

        // ── 4 thẻ kỳ cố định ────────────────────────────────────────────────────
        PeriodStatDTO todayStat = periodStat(today, today);
        PeriodStatDTO weekStat  = periodStat(today.with(DayOfWeek.MONDAY), today);
        PeriodStatDTO monthStat = periodStat(today.withDayOfMonth(1), today);
        PeriodStatDTO yearStat  = periodStat(today.withDayOfYear(1), today);

        // ── Biểu đồ doanh thu ────────────────────────────────────────────────────
        List<Object[]> chartRows = queryChartRows(period, fromDt, toDt);
        List<String>     chartLabels   = new ArrayList<>();
        List<BigDecimal> chartRevenues = new ArrayList<>();
        for (Object[] row : chartRows) {
            chartLabels.add(row[0] != null ? row[0].toString() : "");
            chartRevenues.add(row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO);
        }

        // ── Donut trạng thái đơn hàng ────────────────────────────────────────────
        Map<String, Long> statusMap = buildStatusMap(fromDt, toDt);
        List<String>  donutLabels = new ArrayList<>();
        List<Long>    donutData   = new ArrayList<>();
        List<String>  donutColors = new ArrayList<>();
        for (String key : STATUS_ORDER) {
            Long cnt = statusMap.getOrDefault(key, 0L);
            if (cnt > 0) {
                donutLabels.add(STATUS_LABEL.getOrDefault(key, key));
                donutData.add(cnt);
                donutColors.add(STATUS_COLOR.getOrDefault(key, "#aaa"));
            }
        }

        // ── Bảng chi tiết 4 kỳ + tăng trưởng ───────────────────────────────────
        List<ThongKeChiTietDTO> chiTiet = buildChiTiet(today);

        // ── Top sản phẩm (30 ngày gần nhất, cố định) ────────────────────────────
        LocalDate topTo   = today;
        LocalDate topFrom = today.minusDays(29);
        List<TopSanPhamDTO> topSanPham = mapTopSanPham(
                chiTietHoaDonRepository.findTopSanPhamBanChay(
                        topFrom.atStartOfDay(), topTo.plusDays(1).atStartOfDay()));

        // ── Nhãn kỳ hiển thị ─────────────────────────────────────────────────────
        String periodDisplay = buildPeriodDisplay(period, from, to, today);

        return ThongKeDTO.builder()
                .selectedPeriod(period)
                .periodDisplay(periodDisplay)
                .ngayHienTai(today)
                .tongSoDon(tongSoDon)
                .tongDoanhThu(tongDoanhThu)
                .doanhThuThucTe(doanhThuThucTe)
                .doanhThuDuKien(doanhThuDuKien)
                .today(todayStat)
                .week(weekStat)
                .month(monthStat)
                .year(yearStat)
                .chartLabelsJson(toJson(chartLabels))
                .chartDoanhThuJson(toJson(chartRevenues))
                .statusLabelsJson(toJson(donutLabels))
                .statusDataJson(toJson(donutData))
                .statusColorsJson(toJson(donutColors))
                .chiTiet(chiTiet)
                .topSanPham(topSanPham)
                .topFrom(topFrom)
                .topTo(topTo)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /** Trả về [from, to] (inclusive) theo period */
    private LocalDate[] periodRange(String period, LocalDate today) {
        return switch (period) {
            case "week"  -> new LocalDate[]{ today.with(DayOfWeek.MONDAY), today };
            case "month" -> new LocalDate[]{ today.withDayOfMonth(1), today };
            case "year"  -> new LocalDate[]{ today.withDayOfYear(1), today };
            default      -> new LocalDate[]{ today, today }; // "today"
        };
    }

    /** Truy vấn chart phù hợp với kỳ */
    private List<Object[]> queryChartRows(String period, LocalDateTime from, LocalDateTime to) {
        return switch (period) {
            case "year"  -> hoaDonRepository.findDoanhThuTheoThang(from, to);
            case "today" -> hoaDonRepository.findDoanhThuTheoGio(from, to);
            default      -> hoaDonRepository.findDoanhThuTheoNgay(from, to); // week, month
        };
    }

    /** Tính PeriodStatDTO cho một khoảng [from, to] */
    private PeriodStatDTO periodStat(LocalDate from, LocalDate to) {
        LocalDateTime f = from.atStartOfDay();
        LocalDateTime t = to.plusDays(1).atStartOfDay();
        BigDecimal dt = nvl(hoaDonRepository.sumDoanhThu(f, t));
        Long don      = nvl(hoaDonRepository.countDonHoanThanh(f, t));
        Long sp       = nvl(chiTietHoaDonRepository.sumSoLuongBan(f, t));
        return new PeriodStatDTO(dt, don, sp);
    }

    /** Map trang_thai → count cho donut chart */
    private Map<String, Long> buildStatusMap(LocalDateTime from, LocalDateTime to) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : hoaDonRepository.countByTrangThai(from, to)) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long   count  = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            map.merge(status, count, Long::sum);
        }
        return map;
    }

    /** Xây dựng bảng chi tiết 4 kỳ với % tăng trưởng */
    private List<ThongKeChiTietDTO> buildChiTiet(LocalDate today) {
        // Kỳ hiện tại
        BigDecimal todayRev  = periodRevenue(today, today);
        Long       todayDon  = periodOrders(today, today);
        BigDecimal weekRev   = periodRevenue(today.with(DayOfWeek.MONDAY), today);
        Long       weekDon   = periodOrders(today.with(DayOfWeek.MONDAY), today);
        BigDecimal monthRev  = periodRevenue(today.withDayOfMonth(1), today);
        Long       monthDon  = periodOrders(today.withDayOfMonth(1), today);
        BigDecimal yearRev   = periodRevenue(today.withDayOfYear(1), today);
        Long       yearDon   = periodOrders(today.withDayOfYear(1), today);

        // Kỳ trước để so sánh
        BigDecimal prevTodayRev = periodRevenue(today.minusDays(1), today.minusDays(1));
        BigDecimal prevWeekRev  = periodRevenue(
                today.minusWeeks(1).with(DayOfWeek.MONDAY),
                today.minusWeeks(1).with(DayOfWeek.SUNDAY));
        BigDecimal prevMonthRev = periodRevenue(
                today.minusMonths(1).withDayOfMonth(1),
                today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()));
        BigDecimal prevYearRev  = periodRevenue(
                today.minusYears(1).withDayOfYear(1),
                today.minusYears(1).withDayOfYear(today.minusYears(1).lengthOfYear()));

        return List.of(
            chiTiet("Hôm nay",   todayRev, todayDon, growth(todayRev, prevTodayRev)),
            chiTiet("Tuần này",  weekRev,  weekDon,  growth(weekRev,  prevWeekRev)),
            chiTiet("Tháng này", monthRev, monthDon, growth(monthRev, prevMonthRev)),
            chiTiet("Năm nay",   yearRev,  yearDon,  growth(yearRev,  prevYearRev))
        );
    }

    private ThongKeChiTietDTO chiTiet(String label, BigDecimal rev, Long don, BigDecimal growth) {
        BigDecimal avg = (don != null && don > 0)
                ? rev.divide(BigDecimal.valueOf(don), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new ThongKeChiTietDTO(label, rev, don, avg, growth);
    }

    private BigDecimal periodRevenue(LocalDate from, LocalDate to) {
        return nvl(hoaDonRepository.sumDoanhThu(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    private Long periodOrders(LocalDate from, LocalDate to) {
        return nvl(hoaDonRepository.countDonHoanThanh(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay()));
    }

    /** % tăng trưởng = (current - prev) / prev * 100; null nếu prev = 0 */
    private BigDecimal growth(BigDecimal current, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) return null;
        return current.subtract(prev)
                      .multiply(BigDecimal.valueOf(100))
                      .divide(prev, 2, RoundingMode.HALF_UP);
    }

    /** Nhãn hiển thị kỳ (ví dụ: "HÔM NAY (2026-03-10)") */
    private String buildPeriodDisplay(String period, LocalDate from, LocalDate to, LocalDate today) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return switch (period) {
            case "week"  -> "TUẦN NÀY (" + from.format(fmt) + " – " + to.format(fmt) + ")";
            case "month" -> "THÁNG NÀY (" + from.format(fmt) + " – " + to.format(fmt) + ")";
            case "year"  -> "NĂM NAY (" + today.getYear() + ")";
            default      -> "HÔM NAY (" + today.format(fmt) + ")";
        };
    }

    // ── Mapping helpers ─────────────────────────────────────────────────────────

    private List<TopSanPhamDTO> mapTopSanPham(List<Object[]> rows) {
        List<TopSanPhamDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long       id       = row[0] != null ? ((Number) row[0]).longValue() : null;
            String     ten      = row[1] != null ? row[1].toString() : "";
            String     mau      = row[2] != null ? row[2].toString() : "";
            String     kt       = row[3] != null ? row[3].toString() : "";
            String     ma       = row[4] != null ? row[4].toString() : "";
            String     anh      = row[5] != null ? row[5].toString() : "";
            Long       slBan    = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            BigDecimal doanhThu = row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO;
            BigDecimal giaBan   = slBan > 0
                    ? doanhThu.divide(BigDecimal.valueOf(slBan), 0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            result.add(new TopSanPhamDTO(id, ten, mau, kt, ma, anh, slBan, doanhThu, giaBan));
        }
        return result;
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (JsonProcessingException e) { return "[]"; }
    }

    private static BigDecimal nvl(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private static Long nvl(Long v)             { return v != null ? v : 0L; }
}