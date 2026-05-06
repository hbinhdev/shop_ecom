package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/thuoc-tinh")
public class ThuocTinhController {

    @Autowired private MauSacRepository mauSacRepo;
    @Autowired private KichThuocRepository kichThuocRepo;
    @Autowired private DanhMucRepository danhMucRepo;
    @Autowired private ThuongHieuRepository thuongHieuRepo;
    @Autowired private KieuDangRepository kieuDangRepo;
    @Autowired private ChatLieuRepository chatLieuRepo;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("listMauSac",    mauSacRepo.findAll());
        model.addAttribute("listKichThuoc", kichThuocRepo.findAll());
        model.addAttribute("listDanhMuc",   danhMucRepo.findAll());
        model.addAttribute("listThuongHieu",thuongHieuRepo.findAll());
        model.addAttribute("listKieuDang",  kieuDangRepo.findAll());
        model.addAttribute("listChatLieu",  chatLieuRepo.findAll());
        return "admin/thuoc-tinh/index";
    }

    // ============================
    // MÀU SẮC
    // ============================
    @PostMapping("/mau-sac/add")
    @ResponseBody
    public ResponseEntity<?> addMauSac(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (mauSacRepo.findAll().stream().anyMatch(m -> m.getTenMauSac().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên màu sắc đã tồn tại!"));
        }
        MauSac m = new MauSac();
        m.setTenMauSac(ten.trim());
        m.setXoaMem(false);
        m.setNgayTao(LocalDateTime.now());
        m.setNguoiTao("Admin");
        mauSacRepo.save(m);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm màu sắc!"));
    }

    @PostMapping("/mau-sac/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleMauSac(@PathVariable Long id) {
        mauSacRepo.findById(id).ifPresent(m -> {
            m.setXoaMem(m.getXoaMem() == null || Boolean.FALSE.equals(m.getXoaMem()));
            m.setNgaySuaCuoi(LocalDateTime.now());
            mauSacRepo.save(m);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/mau-sac/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateMauSac(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (mauSacRepo.findAll().stream().anyMatch(m -> !m.getId().equals(id) && m.getTenMauSac().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên màu sắc đã tồn tại!"));
        }
        return mauSacRepo.findById(id).map(m -> {
            m.setTenMauSac(ten.trim());
            m.setNgaySuaCuoi(LocalDateTime.now());
            mauSacRepo.save(m);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // KÍCH THƯỚC
    // ============================
    @PostMapping("/kich-thuoc/add")
    @ResponseBody
    public ResponseEntity<?> addKichThuoc(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        try {
            int size = Integer.parseInt(ten.trim());
            if (size < 36 || size > 45) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kích thước phải từ 36 đến 45!"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kích thước phải là số!"));
        }
        if (kichThuocRepo.findAll().stream().anyMatch(k -> k.getTenKichThuoc().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên kích thước đã tồn tại!"));
        }
        KichThuoc k = new KichThuoc();
        k.setTenKichThuoc(ten.trim());
        k.setXoaMem(false);
        k.setNgayTao(LocalDateTime.now());
        k.setNguoiTao("Admin");
        kichThuocRepo.save(k);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm kích thước!"));
    }

    @PostMapping("/kich-thuoc/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleKichThuoc(@PathVariable Long id) {
        kichThuocRepo.findById(id).ifPresent(k -> {
            k.setXoaMem(k.getXoaMem() == null || Boolean.FALSE.equals(k.getXoaMem()));
            k.setNgaySuaCuoi(LocalDateTime.now());
            kichThuocRepo.save(k);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/kich-thuoc/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateKichThuoc(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        try {
            int size = Integer.parseInt(ten.trim());
            if (size < 36 || size > 45) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kích thước phải từ 36 đến 45!"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Kích thước phải là số!"));
        }
        if (kichThuocRepo.findAll().stream().anyMatch(k -> !k.getId().equals(id) && k.getTenKichThuoc().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên kích thước đã tồn tại!"));
        }
        return kichThuocRepo.findById(id).map(k -> {
            k.setTenKichThuoc(ten.trim());
            k.setNgaySuaCuoi(LocalDateTime.now());
            kichThuocRepo.save(k);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // DANH MỤC
    // ============================
    @PostMapping("/danh-muc/add")
    @ResponseBody
    public ResponseEntity<?> addDanhMuc(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (danhMucRepo.findAll().stream().anyMatch(d -> d.getTenDanhMuc().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên danh mục đã tồn tại!"));
        }
        DanhMuc d = new DanhMuc();
        d.setTenDanhMuc(ten.trim());
        d.setXoaMem(false);
        d.setNgayTao(LocalDateTime.now());
        d.setNguoiTao("Admin");
        danhMucRepo.save(d);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm danh mục!"));
    }

    @PostMapping("/danh-muc/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleDanhMuc(@PathVariable Long id) {
        danhMucRepo.findById(id).ifPresent(d -> {
            d.setXoaMem(d.getXoaMem() == null || Boolean.FALSE.equals(d.getXoaMem()));
            d.setNgaySuaCuoi(LocalDateTime.now());
            danhMucRepo.save(d);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/danh-muc/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateDanhMuc(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (danhMucRepo.findAll().stream().anyMatch(d -> !d.getId().equals(id) && d.getTenDanhMuc().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên danh mục đã tồn tại!"));
        }
        return danhMucRepo.findById(id).map(d -> {
            d.setTenDanhMuc(ten.trim());
            d.setNgaySuaCuoi(LocalDateTime.now());
            danhMucRepo.save(d);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // THƯƠNG HIỆU
    // ============================
    @PostMapping("/thuong-hieu/add")
    @ResponseBody
    public ResponseEntity<?> addThuongHieu(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (thuongHieuRepo.findAll().stream().anyMatch(t -> t.getTenThuongHieu().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên thương hiệu đã tồn tại!"));
        }
        ThuongHieu t = new ThuongHieu();
        t.setTenThuongHieu(ten.trim());
        t.setXoaMem(false);
        t.setNgayTao(LocalDateTime.now());
        t.setNguoiTao("Admin");
        thuongHieuRepo.save(t);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm thương hiệu!"));
    }

    @PostMapping("/thuong-hieu/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleThuongHieu(@PathVariable Long id) {
        thuongHieuRepo.findById(id).ifPresent(t -> {
            t.setXoaMem(t.getXoaMem() == null || Boolean.FALSE.equals(t.getXoaMem()));
            t.setNgaySuaCuoi(LocalDateTime.now());
            thuongHieuRepo.save(t);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/thuong-hieu/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateThuongHieu(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (thuongHieuRepo.findAll().stream().anyMatch(t -> !t.getId().equals(id) && t.getTenThuongHieu().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên thương hiệu đã tồn tại!"));
        }
        return thuongHieuRepo.findById(id).map(t -> {
            t.setTenThuongHieu(ten.trim());
            t.setNgaySuaCuoi(LocalDateTime.now());
            thuongHieuRepo.save(t);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // KIỂU DÁNG
    // ============================
    @PostMapping("/kieu-dang/add")
    @ResponseBody
    public ResponseEntity<?> addKieuDang(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (kieuDangRepo.findAll().stream().anyMatch(k -> k.getTenKieuDang().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên kiểu dáng đã tồn tại!"));
        }
        KieuDang k = new KieuDang();
        k.setTenKieuDang(ten.trim());
        k.setXoaMem(false);
        k.setNgayTao(LocalDateTime.now());
        k.setNguoiTao("Admin");
        kieuDangRepo.save(k);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm kiểu dáng!"));
    }

    @PostMapping("/kieu-dang/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleKieuDang(@PathVariable Long id) {
        kieuDangRepo.findById(id).ifPresent(k -> {
            k.setXoaMem(k.getXoaMem() == null || Boolean.FALSE.equals(k.getXoaMem()));
            k.setNgaySuaCuoi(LocalDateTime.now());
            kieuDangRepo.save(k);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/kieu-dang/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateKieuDang(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (kieuDangRepo.findAll().stream().anyMatch(k -> !k.getId().equals(id) && k.getTenKieuDang().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên kiểu dáng đã tồn tại!"));
        }
        return kieuDangRepo.findById(id).map(k -> {
            k.setTenKieuDang(ten.trim());
            k.setNgaySuaCuoi(LocalDateTime.now());
            kieuDangRepo.save(k);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // CHẤT LIỆU
    // ============================
    @PostMapping("/chat-lieu/add")
    @ResponseBody
    public ResponseEntity<?> addChatLieu(@RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (chatLieuRepo.findAll().stream().anyMatch(c -> c.getTenChatLieu().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên chất liệu đã tồn tại!"));
        }
        ChatLieu c = new ChatLieu();
        c.setTenChatLieu(ten.trim());
        c.setXoaMem(false);
        c.setNgayTao(LocalDateTime.now());
        c.setNguoiTao("Admin");
        chatLieuRepo.save(c);
        return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm chất liệu!"));
    }

    @PostMapping("/chat-lieu/toggle/{id}")
    @ResponseBody
    public ResponseEntity<?> toggleChatLieu(@PathVariable Long id) {
        chatLieuRepo.findById(id).ifPresent(c -> {
            c.setXoaMem(c.getXoaMem() == null || Boolean.FALSE.equals(c.getXoaMem()));
            c.setNgaySuaCuoi(LocalDateTime.now());
            chatLieuRepo.save(c);
        });
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/chat-lieu/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateChatLieu(@PathVariable Long id, @RequestParam String ten) {
        if (ten == null || ten.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên không được để trống!"));
        if (chatLieuRepo.findAll().stream().anyMatch(c -> !c.getId().equals(id) && c.getTenChatLieu().equalsIgnoreCase(ten.trim()))) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên chất liệu đã tồn tại!"));
        }
        return chatLieuRepo.findById(id).map(c -> {
            c.setTenChatLieu(ten.trim());
            c.setNgaySuaCuoi(LocalDateTime.now());
            chatLieuRepo.save(c);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }
}