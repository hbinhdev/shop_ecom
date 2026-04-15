package com.example.datn_shop_ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodStatDTO {
    private BigDecimal doanhThu = BigDecimal.ZERO;
    private Long soDon = 0L;
    private Long soSanPham = 0L;
}