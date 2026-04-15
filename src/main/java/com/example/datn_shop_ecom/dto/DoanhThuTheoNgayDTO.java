package com.example.datn_shop_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoanhThuTheoNgayDTO {
    private String ngay;       // YYYY-MM-DD
    private BigDecimal doanhThu;
    private Long soDon;
}
