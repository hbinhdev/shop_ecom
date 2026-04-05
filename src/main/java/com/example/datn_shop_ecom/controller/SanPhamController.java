package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.SanPham;
import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import com.example.datn_shop_ecom.entity.HinhAnh;
import com.example.datn_shop_ecom.repository.ChatLieuRepository;
import com.example.datn_shop_ecom.repository.KichThuocRepository;
import com.example.datn_shop_ecom.repository.LoaiSanRepository;
import com.example.datn_shop_ecom.repository.MauSacRepository;
import com.example.datn_shop_ecom.repository.ThuongHieuRepository;
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
    private LoaiSanRepository loaiSanRepo;

    @Autowired
    private ThuongHieuRepository thuongHieuRepo;

    @Autowired
    private ChatLieuRepository chatLieuRepo;

    @GetMapping("/bien-the")
    public String bienThe(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long idMauSac,
            @RequestParam(required = false) Long idKichThuoc,
            @RequestParam(required = false) Long idLoaiSan,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false, defaultValue = "20000000") BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "") String trThai,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        // Ưu tiên sản phẩm mới cập nhật lên ĐẦU và gom nhóm chúng lại
        Pageable pageable = PageRequest.of(page, 10, Sort.by("sanPham.ngaySuaCuoi").descending().and(Sort.by("sanPham.id").descending()).and(Sort.by("id").descending()));
        Page<SanPhamChiTiet> spctPage = spctService.filterVariantPage(search, idMauSac, idKichThuoc, idLoaiSan, minPrice, maxPrice, trThai, pageable);
        
        // Chuyển sang danh sách Map cực kỳ an toàn để View không bao giờ bị lỗi Lazy loading hay Circular reference
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
                vMap.put("loaiSan", v.getLoaiSan() != null ? v.getLoaiSan().getTenLoaiSan() : "N/A");
                vMap.put("soTonKho", v.getSoTonKho() != null ? v.getSoTonKho() : 0);
                vMap.put("giaBan", v.getGiaBan() != null ? v.getGiaBan() : java.math.BigDecimal.ZERO);
                vMap.put("trangThai", v.getTrangThai());
                vMap.put("duongDanAnh", v.getDuongDanAnh());
                vMap.put("idSanPham", v.getSanPham() != null ? v.getSanPham().getId() : null);
                safeVariants.add(vMap);
            }
        }
        
        model.addAttribute("listVariants", safeVariants);
        model.addAttribute("spPage", spctPage);
        model.addAttribute("search", search);
        model.addAttribute("idMauSac", idMauSac);
        model.addAttribute("idKichThuoc", idKichThuoc);
        model.addAttribute("idLoaiSan", idLoaiSan);
        model.addAttribute("minPrice", minPrice != null ? minPrice : BigDecimal.ZERO);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("trThai", trThai);
        
        // Filter Data
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        model.addAttribute("loaiSans", loaiSanRepo.findAll());
        
        return "admin/san-pham/chi-tiet-san-pham";
    }

    @GetMapping
    public String index(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) Long idThuongHieu,
            @RequestParam(required = false) Long idChatLieu,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<SanPham> spPage = sanPhamService.filterSanPham(search, idThuongHieu, idChatLieu, pageable);
        
        model.addAttribute("listSanPham", spPage.getContent());
        model.addAttribute("spPage", spPage);
        model.addAttribute("search", search);
        model.addAttribute("idThuongHieu", idThuongHieu);
        model.addAttribute("idChatLieu", idChatLieu);
        model.addAttribute("currentPage", page);
        
        // Nạp danh mục cho bộ lọc
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll());
        model.addAttribute("chatLieus", chatLieuRepo.findAll());
        
        return "admin/san-pham/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        return "admin/san-pham/create";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SanPham sp = sanPhamService.findById(id);
        model.addAttribute("sp", sp);
        
        // Nạp thêm danh mục để phục vụ "Thêm biến thể nhanh"
        model.addAttribute("mauSacs", mauSacRepo.findAll());
        model.addAttribute("kichThuocs", kichThuocRepo.findAll());
        model.addAttribute("loaiSans", loaiSanRepo.findAll());
        
        return "admin/san-pham/detail";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        SanPham sp = sanPhamService.findById(id);
        model.addAttribute("sp", sp);
        
        // Tạo Map an toàn để truyền cho Javascript (Tránh lỗi tuần hoàn/Lazy loading)
        java.util.Map<String, Object> spData = new java.util.HashMap<>();
        spData.put("id", sp.getId());
        spData.put("tenSanPham", sp.getTenSanPham());
        spData.put("moTa", sp.getMoTa());
        spData.put("idThuongHieu", sp.getThuongHieu() != null ? sp.getThuongHieu().getId() : null);
        spData.put("idXuatXu", sp.getXuatXu() != null ? sp.getXuatXu().getId() : null);
        spData.put("idCoGiay", sp.getCoGiay() != null ? sp.getCoGiay().getId() : null);
        spData.put("idChatLieu", sp.getChatLieu() != null ? sp.getChatLieu().getId() : null);
        spData.put("idViTri", sp.getViTri() != null ? sp.getViTri().getId() : null);
        spData.put("idPhongCach", sp.getPhongCachChoi() != null ? sp.getPhongCachChoi().getId() : null);
        
        model.addAttribute("spData", spData);
        
        // Nạp danh sách biến thể hiện tại
        java.util.List<java.util.Map<String, Object>> variants = new java.util.ArrayList<>();
        if (sp.getDanhSachChiTiet() != null) {
            for (SanPhamChiTiet v : sp.getDanhSachChiTiet()) {
                java.util.Map<String, Object> vMap = new java.util.HashMap<>();
                vMap.put("id", v.getId());
                vMap.put("cId", v.getMauSac() != null ? v.getMauSac().getId() : null);
                vMap.put("cName", v.getMauSac() != null ? v.getMauSac().getTenMauSac() : "N/A");
                vMap.put("sId", v.getKichThuoc() != null ? v.getKichThuoc().getId() : null);
                vMap.put("sName", v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : "N/A");
                vMap.put("grId", v.getLoaiSan() != null ? v.getLoaiSan().getId() : null);
                vMap.put("grName", v.getLoaiSan() != null ? v.getLoaiSan().getTenLoaiSan() : "N/A");
                vMap.put("fId", v.getFormChan() != null ? v.getFormChan().getId() : null);
                vMap.put("fName", v.getFormChan() != null ? v.getFormChan().getTenForm() : "N/A");
                vMap.put("q", v.getSoTonKho());
                vMap.put("p", v.getGiaBan());
                
                String cId = v.getMauSac() != null ? v.getMauSac().getId().toString() : "0";
                String sId = v.getKichThuoc() != null ? v.getKichThuoc().getId().toString() : "0";
                String grId = v.getLoaiSan() != null ? v.getLoaiSan().getId().toString() : "0";
                String fId = v.getFormChan() != null ? v.getFormChan().getId().toString() : "0";
                vMap.put("uid", cId + "-" + sId + "-" + grId + "-" + fId);
                
                variants.add(vMap);
            }
        }
        model.addAttribute("existingVariants", variants);

        // Nạp ảnh theo màu sắc (Lấy từ danh sách HinhAnh của Sản phẩm)
        java.util.Map<String, java.util.List<String>> imagesByColor = new java.util.HashMap<>();
        if (sp.getDanhSachHinhAnh() != null) {
            for (HinhAnh img : sp.getDanhSachHinhAnh()) {
                // Nếu ảnh có liên kết với biến thể thì lấy màu từ biến thể đó
                if (img.getSanPhamChiTiet() != null && img.getSanPhamChiTiet().getMauSac() != null) {
                    String cid = img.getSanPhamChiTiet().getMauSac().getId().toString();
                    java.util.List<String> imgUrls = imagesByColor.computeIfAbsent(cid, k -> new java.util.ArrayList<>());
                    if (!imgUrls.contains(img.getDuongDan())) {
                        imgUrls.add(img.getDuongDan());
                    }
                }
            }
        }
        
        // Bổ sung thêm ảnh trực tiếp từ biến thể (nếu có và chưa có trong HinhAnh)
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long idThuongHieu,
            @RequestParam(required = false) Long idChatLieu) {
        
        java.io.ByteArrayInputStream in = sanPhamService.exportToExcel(search, idThuongHieu, idChatLieu);
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
            @RequestParam(required = false) Long idMauSac,
            @RequestParam(required = false) Long idKichThuoc,
            @RequestParam(required = false) Long idLoaiSan,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String trThai) {
        
        java.io.ByteArrayInputStream in = spctService.exportToExcel(search, idMauSac, idKichThuoc, idLoaiSan, minPrice, maxPrice, trThai);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bien_the_san_pham.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}
