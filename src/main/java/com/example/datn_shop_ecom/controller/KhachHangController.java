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

@Controller
@RequestMapping("/admin/khach-hang")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public String index(Model model, 
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) String gioiTinh,
                        @org.springframework.web.bind.annotation.RequestParam(required = false) Integer trangThai) {
        
        Boolean xoaMem = null;
        if (trangThai != null) {
            xoaMem = (trangThai == 0); // Giả sử 1: Hoạt động (xoaMem=false), 0: Ngừng (xoaMem=true)
            // Sửa lại theo SQL: xoaMem=0 là hoạt động.
            if (trangThai == 1) xoaMem = false; // Hoạt động
            else if (trangThai == 0) xoaMem = true; // Ngừng hoạt động
        }

        model.addAttribute("khachHangs", khachHangService.filterKhachHang(search, gioiTinh, xoaMem));
        model.addAttribute("search", search);
        model.addAttribute("gioiTinh", gioiTinh);
        model.addAttribute("trangThai", trangThai);
        
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
    public String save(@ModelAttribute("khachHang") KhachHang khachHang, RedirectAttributes redirectAttributes) {
        try {
            khachHangService.saveKhachHang(khachHang);
            String message = (khachHang.getId() == null) ? "Thêm khách hàng thành công!" : "Cập nhật khách hàng thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/admin/khach-hang";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return (khachHang.getId() == null) ? "redirect:/admin/khach-hang/create" : "redirect:/admin/khach-hang/edit/" + khachHang.getId();
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
}
