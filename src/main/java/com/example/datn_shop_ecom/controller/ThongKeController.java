package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/thong-ke")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongKeService;

    /**
     * @param period "today" | "week" | "month" | "year" (mặc định "today")
     */
    @GetMapping
    public String thongKe(
            @RequestParam(required = false, defaultValue = "today") String period,
            Model model) {

        model.addAttribute("tk", thongKeService.getThongKe(period));
        return "admin/thong-ke/index";
    }
}