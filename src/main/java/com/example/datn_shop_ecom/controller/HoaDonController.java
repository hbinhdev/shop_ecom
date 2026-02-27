package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String listInvoices(
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer loaiHoaDon,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayTao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<HoaDon> hoaDonPage = hoaDonService.searchInvoices(
                maHoaDon, tenKhachHang, trangThai, loaiHoaDon, ngayTao,
                PageRequest.of(page, size, Sort.by("ngayTao").descending())
        );

        model.addAttribute("hoaDonPage", hoaDonPage);
        model.addAttribute("maHoaDon", maHoaDon);
        model.addAttribute("tenKhachHang", tenKhachHang);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiHoaDon", loaiHoaDon);
        model.addAttribute("ngayTao", ngayTao);

        return "admin/hoa-don/list";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable Long id, Model model) {
        HoaDon hoaDon = hoaDonService.findById(id);
        if (hoaDon == null) {
            return "redirect:/admin/hoa-don";
        }

        model.addAttribute("hd", hoaDon);
        model.addAttribute("listChiTiet", hoaDonService.findDetailByHoaDonId(id));
        model.addAttribute("listLichSu", hoaDonService.findHistoryByHoaDonId(id));
        model.addAttribute("listThanhToan", hoaDonService.findPaymentHistoryByHoaDonId(id));

        return "admin/hoa-don/detail";
    }
}
