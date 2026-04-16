package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.dto.ThongKeDTO;

public interface ThongKeService {
    /**
     * Lấy dữ liệu thống kê theo kỳ.
     *
     * @param period "today" | "week" | "month" | "year"
     */
    ThongKeDTO getThongKe(String period);
}