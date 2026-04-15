package com.example.datn_shop_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeChiTietDTO {
    private String label;           // "Hôm nay", "Tuần này", ...
    private BigDecimal doanhThu;
    private Long soDon;
    private BigDecimal giaTrungBinh; // doanhThu / soDon
    private BigDecimal tangTruong;   // % so với kỳ trước (null nếu không tính được)
}