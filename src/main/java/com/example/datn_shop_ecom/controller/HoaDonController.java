package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.service.ExcelService;
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

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private ExcelService excelService;

    @GetMapping
    public String listInvoices(
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer loaiHoaDon,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<HoaDon> hoaDonPage = hoaDonService.searchInvoices(
                maHoaDon, tenKhachHang, trangThai, loaiHoaDon, ngayBatDau, ngayKetThuc,
                PageRequest.of(page, size, Sort.by("ngayTao").descending())
        );

        model.addAttribute("hoaDonPage", hoaDonPage);
        model.addAttribute("maHoaDon", maHoaDon);
        model.addAttribute("tenKhachHang", tenKhachHang);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiHoaDon", loaiHoaDon);
        model.addAttribute("ngayBatDau", ngayBatDau);
        model.addAttribute("ngayKetThuc", ngayKetThuc);

        return "admin/hoa-don/list";
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> exportExcel(
            @RequestParam(required = false) String maHoaDon,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer loaiHoaDon,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc) {
        
        java.util.List<HoaDon> invoices = hoaDonService.findAllMatchingInvoices(maHoaDon, tenKhachHang, trangThai, loaiHoaDon, ngayBatDau, ngayKetThuc);
        
        try {
            ByteArrayInputStream in = excelService.exportInvoicesToExcel(invoices);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=danh_sach_hoa_don.xlsx");
            
            return org.springframework.http.ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new org.springframework.core.io.InputStreamResource(in));
        } catch (java.io.IOException e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
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

