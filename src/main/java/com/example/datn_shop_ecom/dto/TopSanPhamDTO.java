package com.example.datn_shop_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSanPhamDTO {
    private Long sanPhamChiTietId;
    private String tenSanPham;
    private String tenMauSac;
    private String tenKichThuoc;
    private String maSanPhamChiTiet;
    private String duongDanAnh;
    private Long soLuongBan;
    private BigDecimal doanhThu;
    /** Giá bán trung bình = doanhThu / soLuongBan (tính sẵn để tránh chia trong template) */
    private BigDecimal giaBan;
}
