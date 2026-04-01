package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.PhieuGiamGia;
import com.example.datn_shop_ecom.service.PhieuGiamGiaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/phieu-giam-gia")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService pggService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 5);
        org.springframework.data.domain.Page<PhieuGiamGia> pggPage = pggService.filterPhieuGiamGiaPage(search, startDate, endDate, trangThai, pageable);
        
        model.addAttribute("listPGG", pggPage.getContent());
        model.addAttribute("pggPage", pggPage);
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        
        return "admin/phieu-giam-gia/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("pgg", new PhieuGiamGia());
        return "admin/phieu-giam-gia/create";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("pgg", pggService.findById(id));
        return "admin/phieu-giam-gia/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("pgg") PhieuGiamGia pgg, RedirectAttributes ra) {
        System.out.println("**************************************************");
        System.out.println("CONTROLLER - NHAN DUOC PHIEU: " + pgg.getTenPhieu());
        System.out.println("CONTROLLER - LOAI PHIEU: " + pgg.getLoai());
        System.out.println("**************************************************");
        try {
            pggService.savePGG(pgg);
            ra.addFlashAttribute("success", "Lưu phiếu giảm giá thành công!");
        } catch (Exception e) {
            System.out.println("CONTROLLER - LOI KHI LUU: " + e.getMessage());
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/phieu-giam-gia";
    }

    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            pggService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/soft-delete/{id}")
    @ResponseBody
    public ResponseEntity<?> softDelete(@PathVariable Long id) {
        try {
            pggService.softDelete(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa phiếu giảm giá thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> export(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer trangThai
    ) {
        ByteArrayInputStream in = pggService.exportToExcel(search, startDate, endDate, trangThai);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=phieu_giam_gia.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
