package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/admin/khach-hang")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public String index(Model model, 
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String gioiTinh,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) Integer trangThai,
                        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page) {
        
        Boolean xoaMem = null;
        if (trangThai != null) {
            if (trangThai == 1) xoaMem = false;
            else if (trangThai == 0) xoaMem = true;
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 5);
        org.springframework.data.domain.Page<KhachHang> khachHangPage = khachHangService.filterKhachHangPage(search, gioiTinh, xoaMem, pageable);

        model.addAttribute("khachHangs", khachHangPage.getContent());
        model.addAttribute("khachHangPage", khachHangPage);
        model.addAttribute("search", search);
        model.addAttribute("gioiTinh", gioiTinh);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        
        return "admin/khach-hang/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        KhachHang khachHang = new KhachHang();
        khachHang.getDanhSachDiaChi().add(new com.example.datn_shop_ecom.entity.DiaChi());
        model.addAttribute("khachHang", khachHang);
        return "admin/khach-hang/create";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        KhachHang khachHang = khachHangService.findById(id);
        model.addAttribute("khachHang", khachHang);
        return "admin/khach-hang/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("khachHang") KhachHang khachHang, RedirectAttributes redirectAttributes, Model model) {
        try {
            khachHangService.saveKhachHang(khachHang);
            String message = (khachHang.getId() == null) ? "Thêm khách hàng thành công!" : "Cập nhật khách hàng thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/admin/khach-hang";
        } catch (Exception e) {
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return (khachHang.getId() == null) ? "admin/khach-hang/create" : "admin/khach-hang/edit";
        }
    }

    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            khachHangService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String gioiTinh,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer trangThai) {

        Boolean xoaMem = null;
        if (trangThai != null) {
            if (trangThai == 1) xoaMem = false;
            else if (trangThai == 0) xoaMem = true;
        }

        ByteArrayInputStream in = khachHangService.exportToExcel(search, gioiTinh, xoaMem);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=khach_hang.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}

