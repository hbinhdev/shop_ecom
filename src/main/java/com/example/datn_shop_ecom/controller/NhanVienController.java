package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.repository.VaiTroRepository;
import com.example.datn_shop_ecom.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Controller
@RequestMapping("/admin/nhan-vien")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @GetMapping
    public String index(Model model, 
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Long idVaiTro,
                        @RequestParam(required = false) Integer trangThai,
                        @RequestParam(defaultValue = "0") int page) {
        
        Boolean xoaMem = null;
        if (trangThai != null) {
            if (trangThai == 1) xoaMem = false;
            else if (trangThai == 0) xoaMem = true;
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 5);
        org.springframework.data.domain.Page<NhanVien> nhanVienPage = nhanVienService.filterNhanVienPage(search, idVaiTro, xoaMem, pageable);

        model.addAttribute("nhanViens", nhanVienPage.getContent());
        model.addAttribute("nhanVienPage", nhanVienPage);
        model.addAttribute("vaiTros", vaiTroRepository.findAllByXoaMemFalse());
        model.addAttribute("search", search);
        model.addAttribute("idVaiTro", idVaiTro);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        
        return "admin/nhan-vien/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("nhanVien", new NhanVien());
        model.addAttribute("vaiTros", vaiTroRepository.findAllByXoaMemFalse());
        return "admin/nhan-vien/create";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        NhanVien nhanVien = nhanVienService.findById(id);
        model.addAttribute("nhanVien", nhanVien);
        model.addAttribute("vaiTros", vaiTroRepository.findAllByXoaMemFalse());
        return "admin/nhan-vien/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("nhanVien") NhanVien nhanVien, 
                       org.springframework.validation.BindingResult result,
                       @RequestParam(value = "anhFile", required = false) org.springframework.web.multipart.MultipartFile anhFile,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("nhanVien", nhanVien);
            model.addAttribute("vaiTros", vaiTroRepository.findAllByXoaMemFalse());
            model.addAttribute("error", "Dữ liệu nhập vào chưa đúng: " + result.getFieldError().getDefaultMessage());
            return (nhanVien.getId() == null) ? "admin/nhan-vien/create" : "admin/nhan-vien/edit";
        }
        try {
            nhanVienService.saveNhanVien(nhanVien, anhFile);
            String message = (nhanVien.getId() == null) ? "Thêm nhân viên thành công!" : "Cập nhật nhân viên thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/admin/nhan-vien";
        } catch (Exception e) {
            model.addAttribute("nhanVien", nhanVien);
            model.addAttribute("vaiTros", vaiTroRepository.findAllByXoaMemFalse());
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return (nhanVien.getId() == null) ? "admin/nhan-vien/create" : "admin/nhan-vien/edit";
        }
    }

    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            nhanVienService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long idVaiTro,
            @RequestParam(required = false) Integer trangThai) {

        Boolean xoaMem = null;
        if (trangThai != null) {
            if (trangThai == 1) xoaMem = false;
            else if (trangThai == 0) xoaMem = true;
        }

        ByteArrayInputStream in = nhanVienService.exportToExcel(search, idVaiTro, xoaMem);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=nhan_vien.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}

