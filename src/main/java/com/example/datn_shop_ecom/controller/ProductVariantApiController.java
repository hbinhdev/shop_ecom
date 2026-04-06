package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.HinhAnhRepository;
import com.example.datn_shop_ecom.repository.SanPhamChiTietRepository;
import com.example.datn_shop_ecom.repository.SanPhamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/product-variant")
@Slf4j
public class ProductVariantApiController {

    @Autowired
    private SanPhamRepository sanPhamRepo;

    @Autowired
    private SanPhamChiTietRepository spctRepo;

    @Autowired
    private HinhAnhRepository hinhAnhRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/create-all-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> createAllWithImages(MultipartHttpServletRequest request) {
        try {
            // 1. Parse JSON data
            String jsonData = request.getParameter("data");
            ProductCreatePayload payload = objectMapper.readValue(jsonData, ProductCreatePayload.class);

            // 2. Tạo hoặc Cập nhật Sản phẩm cha
            Long spId = payload.getSanPham().getId();
            SanPham sp;
            if (spId != null) {
                sp = sanPhamRepo.findById(spId).orElse(new SanPham());
                // Nếu là update, không đổi ngày tạo và mã sp
            } else {
                sp = new SanPham();
                sp.setNgayTao(LocalDateTime.now());
                sp.setNguoiTao("Admin");
                sp.setXoaMem(false);
                sp.setMaSanPham("SP" + String.format("%05d", sanPhamRepo.count() + 1));
            }

            sp.setTenSanPham(payload.getSanPham().getTenSanPham());
            sp.setMoTa(payload.getSanPham().getMoTa());
            
            sp.setNgaySuaCuoi(LocalDateTime.now());
            SanPham savedSp = sanPhamRepo.save(sp);

            // 3. Xử lý lưu File vật lý và Map Color -> FileNames
            Map<Long, List<String>> colorToImages = new HashMap<>();
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/san-pham/";
            Files.createDirectories(Paths.get(uploadDir));

            Iterator<String> paramNames = request.getFileNames();
            while (paramNames.hasNext()) {
                String paramName = paramNames.next();
                if (paramName.startsWith("files_")) {
                    Long colorId = Long.parseLong(paramName.replace("files_", ""));
                    List<MultipartFile> files = request.getFiles(paramName);
                    
                    List<String> savedFiles = new ArrayList<>();
                    for (MultipartFile file : files) {
                        if (!file.isEmpty()) {
                            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                            Path filePath = Paths.get(uploadDir).resolve(fileName);
                            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                            savedFiles.add(fileName);

                            if (savedSp.getDuongDanAnh() == null) {
                                savedSp.setDuongDanAnh(fileName);
                                sanPhamRepo.save(savedSp);
                            }
                        }
                    }
                    colorToImages.put(colorId, savedFiles);
                }
            }

            // 4. Tạo hoặc Cập nhật các Biến thể và gắn Hình ảnh
            long spctCount = spctRepo.count();
            for (VariantPayload variant : payload.getBienThes()) {
                SanPhamChiTiet spct;
                if (variant.getId() != null) {
                    spct = spctRepo.findById(variant.getId()).orElse(new SanPhamChiTiet());
                } else {
                    spct = new SanPhamChiTiet();
                    spctCount++;
                    spct.setMaSanPhamChiTiet("SPCT" + String.format("%05d", spctCount));
                    spct.setNgayTao(LocalDateTime.now());
                    spct.setTrangThai("1");
                }

                spct.setSanPham(savedSp);
                spct.setMauSac(MauSac.builder().id(variant.getIdMauSac()).build());
                spct.setKichThuoc(KichThuoc.builder().id(variant.getIdKichThuoc()).build());
                
                spct.setGiaBan(variant.getGiaBan());
                spct.setSoTonKho(variant.getSoLuong());
                spct.setNgaySuaCuoi(LocalDateTime.now());
                
                SanPhamChiTiet savedSpct = spctRepo.save(spct);

                // Gắn tất cả các ảnh thuộc về màu này cho biến thể này
                List<String> imagesOfColor = colorToImages.get(variant.getIdMauSac());
                if (imagesOfColor != null && !imagesOfColor.isEmpty()) {
                    // Set ảnh đầu tiên của màu này làm ảnh đại diện cho biến thể
                    savedSpct.setDuongDanAnh(imagesOfColor.get(0));
                    spctRepo.save(savedSpct);

                    for (String imgPath : imagesOfColor) {
                        HinhAnh ha = HinhAnh.builder()
                                .sanPham(savedSp)
                                .sanPhamChiTiet(savedSpct)
                                .duongDan(imgPath)
                                .laAnhDaiDien(false)
                                .build();
                        hinhAnhRepo.save(ha);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật sản phẩm thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        return sanPhamRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save-simple")
    @Transactional
    public ResponseEntity<?> saveSimpleProduct(@RequestBody SanPhamDto dto) {
        try {
            SanPham sp = dto.getId() != null ? sanPhamRepo.findById(dto.getId()).orElse(new SanPham()) : new SanPham();
            sp.setTenSanPham(dto.getTenSanPham());
            sp.setMoTa(dto.getMoTa());

            if(dto.getId() == null) {
                sp.setNgayTao(LocalDateTime.now());
                sp.setXoaMem(false);
                sp.setMaSanPham("SP" + String.format("%05d", sanPhamRepo.count() + 1));
            }
            sanPhamRepo.save(sp);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/variant/save")
    @Transactional
    public ResponseEntity<?> saveVariant(@RequestBody VariantPayload dto) {
        try {
            SanPhamChiTiet spct;
            if (dto.getId() != null) {
                spct = spctRepo.findById(dto.getId()).orElse(new SanPhamChiTiet());
            } else {
                spct = new SanPhamChiTiet();
                // Chỉ set các thông tin định danh khi thêm mới
                if (dto.getIdSanPham() != null) spct.setSanPham(sanPhamRepo.findById(dto.getIdSanPham()).orElse(null));
                if (dto.getIdMauSac() != null) spct.setMauSac(MauSac.builder().id(dto.getIdMauSac()).build());
                if (dto.getIdKichThuoc() != null) spct.setKichThuoc(KichThuoc.builder().id(dto.getIdKichThuoc()).build());
                
                spct.setMaSanPhamChiTiet("SPCT" + String.format("%05d", spctRepo.count() + 1));
                spct.setTrangThai("1");
                spct.setNgayTao(LocalDateTime.now());
            }
            
            spct.setGiaBan(dto.getGiaBan());
            spct.setSoTonKho(dto.getSoLuong());
            
            spctRepo.save(spct);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper DTOs
    @Data
    public static class ProductCreatePayload {
        private SanPhamDto sanPham;
        private List<VariantPayload> bienThes;
    }

    @Data
    public static class SanPhamDto {
        private Long id;
        private String tenSanPham;
        private String moTa;
    }

    @Data
    public static class VariantPayload {
        private Long id;
        private Long idSanPham;
        private Long idMauSac;
        private Long idKichThuoc;
        private Integer soLuong;
        private BigDecimal giaBan;
        private String anhMauSac;
    }
}
