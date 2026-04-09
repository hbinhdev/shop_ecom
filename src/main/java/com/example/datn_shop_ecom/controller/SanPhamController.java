package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.SanPham;
import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import com.example.datn_shop_ecom.entity.HinhAnh;

import com.example.datn_shop_ecom.repository.*;
import com.example.datn_shop_ecom.service.SanPhamChiTietService;
import com.example.datn_shop_ecom.service.SanPhamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private SanPhamChiTietService spctService;

    @Autowired
    private MauSacRepository mauSacRepo;

    @Autowired
    private KichThuocRepository kichThuocRepo;

    @Autowired
    private DanhMucRepository danhMucRepo;

    @Autowired
    private ThuongHieuRepository thuongHieuRepo;

    @Autowired
    private KieuDangRepository kieuDangRepo;

    @Autowired
    private ChatLieuRepository chatLieuRepo;

    @Autowired
    private HinhAnhRepository hinhAnhRepo;

    @GetMapping("/bien-the")
    public String bienThe(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long idSanPham,
            @RequestParam(required = false) Long idMauSac,
            @RequestParam(required = false) Long idKichThuoc,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false, defaultValue = "20000000") BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "") String trThai,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        
        Pageable pageable = PageRequest.of(page, 10, Sort.by("sanPham.ngaySuaCuoi").descending().and(Sort.by("sanPham.id").descending()).and(Sort.by("id").descending()));
        Page<SanPhamChiTiet> spctPage = spctService.filterVariantPage(search, idSanPham, idMauSac, idKichThuoc, minPrice, maxPrice, trThai, pageable);
        
        
        java.util.List<Long> variantIds = new java.util.ArrayList<>();
        if (spctPage != null) variantIds = spctPage.getContent().stream().map(v -> v.getId()).collect(java.util.stream.Collectors.toList());
        
        
        java.util.Map<Long, String> imgMap = new java.util.HashMap<>();
        if (!variantIds.isEmpty()) {
            java.util.List<HinhAnh> allImg = hinhAnhRepo.findBySanPhamChiTietIdIn(variantIds);
            for (HinhAnh h : allImg) {
                if (h.getSanPhamChiTiet() != null && !imgMap.containsKey(h.getSanPhamChiTiet().getId())) {
                    imgMap.put(h.getSanPhamChiTiet().getId(), h.getDuongDan());
                }
            }
        }

        
        java.util.List<java.util.Map<String, Object>> safeVariants = new java.util.ArrayList<>();
        if (spctPage != null) {
            for (SanPhamChiTiet v : spctPage.getContent()) {
                java.util.Map<String, Object> vMap = new java.util.HashMap<>();
                vMap.put("id", v.getId());
                vMap.put("maSanPhamChiTiet", v.getMaSanPhamChiTiet());
                vMap.put("maSanPham", v.getSanPham() != null ? v.getSanPham().getMaSanPham() : "N/A");
                vMap.put("tenSanPham", v.getSanPham() != null ? v.getSanPham().getTenSanPham() : "N/A");
                vMap.put("mauSac", v.getMauSac() != null ? v.getMauSac().getTenMauSac() : "N/A");
                vMap.put("kichThuoc", v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : "N/A");
                vMap.put("soTonKho", v.getSoTonKho() != null ? v.getSoTonKho() : 0);
                vMap.put("giaBan", v.getGiaBan() != null ? v.getGiaBan() : java.math.BigDecimal.ZERO);
                vMap.put("trangThai", v.getTrangThai());
                
                
                String imgPath = v.getDuongDanAnh(); 
                if ((imgPath == null || imgPath.isEmpty()) && imgMap.containsKey(v.getId())) {
                    imgPath = imgMap.get(v.getId()); 
                }
                if (imgPath == null || imgPath.isEmpty()) {
                    imgPath = (v.getSanPham() != null) ? v.getSanPham().getDuongDanAnh() : null; 
                }
                vMap.put("duongDanAnh", imgPath);
                
                
                vMap.put("idSanPham", v.getSanPham() != null ? v.getSanPham().getId() : null);
                safeVariants.add(vMap);
            }
        }
        
        model.addAttribute("listVariants", safeVariants);
        model.addAttribute("spPage", spctPage);
        model.addAttribute("search", search);
        model.addAttribute("idSanPham", idSanPham);
        model.addAttribute("idMauSac", idMauSac);
        model.addAttribute("idKichThuoc", idKichThuoc);
        model.addAttribute("minPrice", minPrice != null ? minPrice : BigDecimal.ZERO);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("trThai", trThai);
        
        
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        model.addAttribute("sanPhams", sanPhamService.findAll()); 
        
        return "admin/san-pham/chi-tiet-san-pham";
    }

    @GetMapping
    public String index(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) Long idDanhMuc,
            @RequestParam(required = false) Long idThuongHieu,
            @RequestParam(required = false) Long idKieuDang,
            @RequestParam(required = false) Long idChatLieu,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("ngayTao").descending());
        Page<SanPham> spPage = sanPhamService.filterSanPham(search, trangThai, idDanhMuc, idThuongHieu, idKieuDang, idChatLieu, pageable);
        
        model.addAttribute("listSanPham", spPage.getContent());
        model.addAttribute("spPage", spPage);
        model.addAttribute("search", search);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("idDanhMuc", idDanhMuc);
        model.addAttribute("idThuongHieu", idThuongHieu);
        model.addAttribute("idKieuDang", idKieuDang);
        model.addAttribute("idChatLieu", idChatLieu);
        model.addAttribute("currentPage", page);
        
        
        model.addAttribute("listDanhMuc", danhMucRepo.findAll());
        model.addAttribute("listThuongHieu", thuongHieuRepo.findAll());
        model.addAttribute("listKieuDang", kieuDangRepo.findAll());
        model.addAttribute("listChatLieu", chatLieuRepo.findAll());
        

        
        return "admin/san-pham/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("title_focus", "Thiết lập sản phẩm & Biến thể");
        model.addAttribute("hideLayout", true);
        return "admin/san-pham/create";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SanPham sp = sanPhamService.findById(id);
        model.addAttribute("sp", sp);
        
        
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        
        return "admin/san-pham/detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        SanPham sp = sanPhamService.findById(id);
        model.addAttribute("sp", sp);
        model.addAttribute("title_focus", "Chỉnh sửa sản phẩm: " + sp.getTenSanPham());
        model.addAttribute("hideLayout", true);
        
        
        java.util.Map<String, Object> spData = new java.util.HashMap<>();
        spData.put("id", sp.getId());
        spData.put("tenSanPham", sp.getTenSanPham());
        spData.put("moTa", sp.getMoTa());
        spData.put("idDanhMuc", sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : "");
        spData.put("idThuongHieu", sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : "");
        spData.put("idKieuDang", sp.getKieuDang() != null ? sp.getKieuDang().getId() : "");
        spData.put("idChatLieu", sp.getChatLieu() != null ? sp.getChatLieu().getId() : "");
        
        model.addAttribute("spData", spData);
        
        
        java.util.List<java.util.Map<String, Object>> variants = new java.util.ArrayList<>();
        if (sp.getDanhSachChiTiet() != null) {
            for (SanPhamChiTiet v : sp.getDanhSachChiTiet()) {
                java.util.Map<String, Object> vMap = new java.util.HashMap<>();
                vMap.put("id", v.getId());
                vMap.put("cId", v.getMauSac() != null ? v.getMauSac().getId() : null);
                vMap.put("cName", v.getMauSac() != null ? v.getMauSac().getTenMauSac() : "N/A");
                vMap.put("sId", v.getKichThuoc() != null ? v.getKichThuoc().getId() : null);
                vMap.put("sName", v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : "N/A");
                vMap.put("q", v.getSoTonKho());
                vMap.put("p", v.getGiaBan());
                
                String cId = v.getMauSac() != null ? v.getMauSac().getId().toString() : "0";
                String sId = v.getKichThuoc() != null ? v.getKichThuoc().getId().toString() : "0";
                vMap.put("uid", cId + "-" + sId);
                
                variants.add(vMap);
            }
        }
        model.addAttribute("existingVariants", variants);

        
        java.util.Map<String, java.util.List<String>> imagesByColor = new java.util.HashMap<>();
        if (sp.getDanhSachHinhAnh() != null) {
            for (HinhAnh img : sp.getDanhSachHinhAnh()) {
                
                if (img.getSanPhamChiTiet() != null && img.getSanPhamChiTiet().getMauSac() != null) {
                    String cid = img.getSanPhamChiTiet().getMauSac().getId().toString();
                    java.util.List<String> imgUrls = imagesByColor.computeIfAbsent(cid, k -> new java.util.ArrayList<>());
                    if (!imgUrls.contains(img.getDuongDan())) {
                        imgUrls.add(img.getDuongDan());
                    }
                }
            }
        }
        
        
        if (sp.getDanhSachChiTiet() != null) {
            for (SanPhamChiTiet v : sp.getDanhSachChiTiet()) {
                if (v.getMauSac() != null && v.getDuongDanAnh() != null && !v.getDuongDanAnh().isEmpty()) {
                    String cid = v.getMauSac().getId().toString();
                    java.util.List<String> imgUrls = imagesByColor.computeIfAbsent(cid, k -> new java.util.ArrayList<>());
                    if (!imgUrls.contains(v.getDuongDanAnh())) {
                        imgUrls.add(v.getDuongDanAnh());
                    }
                }
            }
        }
        model.addAttribute("existingImages", imagesByColor);

        return "admin/san-pham/edit";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> getSanPhamApi(@PathVariable Long id) {
        try {
            SanPham sp = sanPhamService.findById(id);
            return ResponseEntity.ok(Map.of(
                "id", sp.getId() != null ? sp.getId() : "",
                "tenSanPham", sp.getTenSanPham() != null ? sp.getTenSanPham() : "",
                "moTa", sp.getMoTa() != null ? sp.getMoTa() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<?> saveApi(@RequestBody SanPham sanPham) {
        try {
            sanPhamService.saveSanPham(sanPham);
            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            sanPhamService.toggleStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/variant/toggle-status/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleVariantStatus(@PathVariable Long id) {
        try {
            spctService.toggleVariantStatus(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật trạng thái biến thể thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportExcel(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(required = false) Long idDanhMuc,
            @RequestParam(required = false) Long idThuongHieu,
            @RequestParam(required = false) Long idKieuDang,
            @RequestParam(required = false) Long idChatLieu) {
        
        java.io.ByteArrayInputStream in = sanPhamService.exportToExcel(search, trangThai, idDanhMuc, idThuongHieu, idKieuDang, idChatLieu);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=san_pham.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }

    @GetMapping("/export-variants")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportVariantsExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long idSanPham,
            @RequestParam(required = false) Long idMauSac,
            @RequestParam(required = false) Long idKichThuoc,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String trThai) {
        
        java.io.ByteArrayInputStream in = spctService.exportToExcel(search, idSanPham, idMauSac, idKichThuoc, minPrice, maxPrice, trThai);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bien_the_san_pham.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}

