package com.example.datn_shop_ecom.controller;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.service.BanHangService;
import com.example.datn_shop_ecom.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class BanHangController {

    @Autowired private HoaDonService hoaDonService;
    @Autowired private BanHangService banHangService;

    // ================================================================
    //  TRANG POS
    // ================================================================
    @GetMapping("/admin/ban-hang")
    public String posPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<HoaDon> pendingList = hoaDonService.findAllPendingPOS();
        model.addAttribute("pendingList", pendingList);
        model.addAttribute("currentUser", userDetails != null ? userDetails.getUsername() : "");
        return "admin/ban-hang/index";
    }

    // ================================================================
    //  API: HOÁ ĐƠN
    // ================================================================

    /** Tạo hóa đơn chờ mới */
    @PostMapping("/api/hoadon/create-pending")
    @ResponseBody
    public ResponseEntity<?> createPending(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        try {
            HoaDon hd = hoaDonService.createPendingInvoice(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("id", hd.getId(), "maHoaDon", hd.getMaHoaDon()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Lấy danh sách giỏ hàng của một hóa đơn */
    @GetMapping("/api/hoadon/{id}/items")
    @ResponseBody
    public ResponseEntity<?> getItems(@PathVariable Long id) {
        try {
            List<ChiTietHoaDon> items = banHangService.getCartItems(id);
            List<CartItemDTO> result = items.stream().map(CartItemDTO::from).toList();
            HoaDon hd = hoaDonService.findById(id);
            KhachHang kh = hd != null ? hd.getKhachHang() : null;
            PhieuGiamGia p = hd != null ? hd.getPhieuGiamGia() : null;
            return ResponseEntity.ok(Map.of(
                "items", result,
                "tongTien", hd != null && hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO,
                "tongTienAfterGiam", hd != null && hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam() : BigDecimal.ZERO,
                "tienPhieuGiamGia", hd != null && hd.getTienPhieuGiamGia() != null ? hd.getTienPhieuGiamGia() : BigDecimal.ZERO,
                "khachHang", kh != null ? KhachHangDTO.from(kh) : "",
                "phieuGiamGia", p != null ? VoucherDTO.from(p) : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Thêm sản phẩm vào hóa đơn */
    @PostMapping("/api/hoadon/{id}/add-product")
    @ResponseBody
    public ResponseEntity<?> addProduct(
            @PathVariable Long id,
            @RequestBody AddProductRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ChiTietHoaDon ct = banHangService.addProduct(id, req.spctId(), req.soLuong(), userDetails.getUsername());
            HoaDon hd = hoaDonService.findById(id);
            return ResponseEntity.ok(Map.of(
                "item", CartItemDTO.from(ct),
                "tongTien", hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO,
                "tongTienAfterGiam", hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam() : BigDecimal.ZERO
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Cập nhật số lượng một dòng chi tiết */
    @PatchMapping("/api/hoadon/items/{chiTietId}/quantity")
    @ResponseBody
    public ResponseEntity<?> updateQty(
            @PathVariable Long chiTietId,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            int soLuong = body.getOrDefault("soLuong", 1);
            ChiTietHoaDon ct = banHangService.updateQuantity(chiTietId, soLuong, userDetails.getUsername());
            HoaDon hd = hoaDonService.findById(ct.getHoaDon().getId());
            return ResponseEntity.ok(Map.of(
                "item", CartItemDTO.from(ct),
                "tongTien", hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO,
                "tongTienAfterGiam", hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam() : BigDecimal.ZERO
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Xóa một dòng chi tiết */
    @DeleteMapping("/api/hoadon/items/{chiTietId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(
            @PathVariable Long chiTietId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            banHangService.removeItem(chiTietId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    // ================================================================
    //  API: TÌM KIẾM SẢN PHẨM
    // ================================================================

    /** Tìm kiếm sản phẩm theo từ khóa */
    @GetMapping("/api/san-pham/search-pos")
    @ResponseBody
    public ResponseEntity<?> searchProducts(@RequestParam(required = false) String q) {
        List<SanPhamChiTiet> list = banHangService.searchProducts(q);
        return ResponseEntity.ok(list.stream().map(ProductDTO::from).toList());
    }

    /** Tìm sản phẩm theo mã vạch */
    @GetMapping("/api/san-pham/barcode/{maVach}")
    @ResponseBody
    public ResponseEntity<?> findByBarcode(@PathVariable String maVach) {
        SanPhamChiTiet spct = banHangService.findByBarcode(maVach);
        if (spct == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductDTO.from(spct));
    }

    // ================================================================
    //  API: KHÁCH HÀNG
    // ================================================================

    /** Tìm kiếm khách hàng */
    @GetMapping("/api/khach-hang/search-pos")
    @ResponseBody
    public ResponseEntity<?> searchKhachHang(@RequestParam(required = false) String q) {
        List<KhachHang> list = banHangService.searchCustomers(q);
        return ResponseEntity.ok(list.stream().map(KhachHangDTO::from).toList());
    }

    /** Tạo nhanh khách hàng tại quầy */
    @PostMapping("/api/khach-hang/quick-create")
    @ResponseBody
    public ResponseEntity<?> quickCreateKhachHang(
            @RequestBody QuickCreateKhRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            KhachHang kh = banHangService.quickCreateCustomer(
                    req.tenDayDu(), req.soDienThoai(), req.email(),
                    userDetails.getUsername());
            return ResponseEntity.ok(KhachHangDTO.from(kh));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Gán khách hàng vào hóa đơn */
    @PatchMapping("/api/hoadon/{id}/customer")
    @ResponseBody
    public ResponseEntity<?> assignCustomer(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long khId = body.get("khachHangId");
            HoaDon hd = banHangService.assignCustomer(id, khId, userDetails.getUsername());
            return ResponseEntity.ok(buildHoaDonInfo(hd));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Gỡ khách hàng khỏi hóa đơn */
    @DeleteMapping("/api/hoadon/{id}/customer")
    @ResponseBody
    public ResponseEntity<?> removeCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            HoaDon hd = banHangService.removeCustomer(id, userDetails.getUsername());
            return ResponseEntity.ok(buildHoaDonInfo(hd));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================================================================
    //  API: PHIẾU GIẢM GIÁ
    // ================================================================

    /** Lấy danh sách phiếu giảm giá áp dụng được */
    @GetMapping("/api/hoadon/{id}/vouchers")
    @ResponseBody
    public ResponseEntity<?> getVouchers(@PathVariable Long id) {
        HoaDon hd = hoaDonService.findById(id);
        BigDecimal tong = hd != null && hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
        List<PhieuGiamGia> list = banHangService.getApplicableVouchers(tong);
        return ResponseEntity.ok(list.stream().map(VoucherDTO::from).toList());
    }

    /** Áp dụng phiếu giảm giá */
    @PostMapping("/api/hoadon/{id}/apply-voucher")
    @ResponseBody
    public ResponseEntity<?> applyVoucher(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            HoaDon hd = banHangService.applyVoucher(id, body.get("phieuId"), userDetails.getUsername());
            return ResponseEntity.ok(buildHoaDonInfo(hd));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Gỡ phiếu giảm giá */
    @DeleteMapping("/api/hoadon/{id}/voucher")
    @ResponseBody
    public ResponseEntity<?> removeVoucher(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            HoaDon hd = banHangService.removeVoucher(id, userDetails.getUsername());
            return ResponseEntity.ok(buildHoaDonInfo(hd));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================================================================
    //  API: THANH TOÁN
    // ================================================================

    /** Thanh toán và hoàn tất hóa đơn */
    @PostMapping("/api/hoadon/{id}/checkout")
    @ResponseBody
    public ResponseEntity<?> checkout(
            @PathVariable Long id,
            @RequestBody CheckoutRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            HoaDon hd = banHangService.checkout(id, req.tienKhachDua(), req.phuongThuc(), req.loaiDon(), userDetails.getUsername());
            List<ChiTietHoaDon> items = banHangService.getCartItems(id);
            KhachHang kh = hd.getKhachHang();
            return ResponseEntity.ok(Map.of(
                "maHoaDon",          hd.getMaHoaDon(),
                "ngayThanhToan",     hd.getNgayThanhToan().toString(),
                "tongTien",          hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO,
                "tienPhieuGiamGia",  hd.getTienPhieuGiamGia() != null ? hd.getTienPhieuGiamGia() : BigDecimal.ZERO,
                "tongTienAfterGiam", hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam() : BigDecimal.ZERO,
                "tienKhachDua",      hd.getTienKhachDua() != null ? hd.getTienKhachDua() : BigDecimal.ZERO,
                "tienThuaTra",       hd.getTienThuaTra() != null ? hd.getTienThuaTra() : BigDecimal.ZERO,
                "phuongThuc",        hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "",
                "khachHang",         kh != null ? KhachHangDTO.from(kh) : "",
                "items",             items.stream().map(CartItemDTO::from).toList()
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /** Hủy hóa đơn chờ */
    @PostMapping("/api/hoadon/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelInvoice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            banHangService.cancelInvoice(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper: build thông tin hóa đơn trả về frontend
    private Map<String, Object> buildHoaDonInfo(HoaDon hd) {
        KhachHang kh = hd.getKhachHang();
        PhieuGiamGia p = hd.getPhieuGiamGia();
        // Map.of() không chấp nhận null → dùng "" thay cho null
        return Map.of(
            "tongTien",          hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO,
            "tongTienAfterGiam", hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam() : BigDecimal.ZERO,
            "tienPhieuGiamGia",  hd.getTienPhieuGiamGia() != null ? hd.getTienPhieuGiamGia() : BigDecimal.ZERO,
            "khachHang",         kh != null ? KhachHangDTO.from(kh) : "",
            "phieuGiamGia",      p  != null ? VoucherDTO.from(p)    : ""
        );
    }

    // ================================================================
    //  DTOs (records)
    // ================================================================

    record AddProductRequest(Long spctId, int soLuong) {}

    record ProductDTO(
            Long id, String maSPCT, String maVach,
            String tenSanPham, String tenMauSac, String tenKichThuoc,
            BigDecimal giaBan, int soTonKho, String duongDanAnh
    ) {
        static ProductDTO from(SanPhamChiTiet s) {
            return new ProductDTO(
                s.getId(),
                s.getMaSanPhamChiTiet(),
                s.getMaVach(),
                s.getSanPham() != null ? s.getSanPham().getTenSanPham() : "",
                s.getMauSac()  != null ? s.getMauSac().getTenMauSac()   : "",
                s.getKichThuoc() != null ? s.getKichThuoc().getTenKichThuoc() : "",
                s.getGiaBan() != null ? s.getGiaBan() : BigDecimal.ZERO,
                s.getSoTonKho() != null ? s.getSoTonKho() : 0,
                s.getDuongDanAnh()
            );
        }
    }

    record CartItemDTO(
            Long id, Long spctId,
            String tenSanPham, String tenMauSac, String tenKichThuoc,
            BigDecimal gia, int soLuong, BigDecimal thanhTien,
            int soTonKhoCon, String duongDanAnh
    ) {
        static CartItemDTO from(ChiTietHoaDon c) {
            SanPhamChiTiet s = c.getSanPhamChiTiet();
            return new CartItemDTO(
                c.getId(), s.getId(),
                s.getSanPham() != null ? s.getSanPham().getTenSanPham() : "",
                s.getMauSac()  != null ? s.getMauSac().getTenMauSac()   : "",
                s.getKichThuoc() != null ? s.getKichThuoc().getTenKichThuoc() : "",
                c.getGia() != null ? c.getGia() : BigDecimal.ZERO,
                c.getSoLuong() != null ? c.getSoLuong() : 0,
                c.getThanhTien() != null ? c.getThanhTien() : BigDecimal.ZERO,
                s.getSoTonKho() != null ? s.getSoTonKho() : 0,
                s.getDuongDanAnh()
            );
        }
    }

    record KhachHangDTO(Long id, String maKhachHang, String tenDayDu,
                        String soDienThoai, String email, int diemTichLuy) {
        static KhachHangDTO from(KhachHang k) {
            return new KhachHangDTO(
                k.getId(), k.getMaKhachHang(),
                k.getTenDayDu() != null ? k.getTenDayDu() : "",
                k.getSoDienThoai() != null ? k.getSoDienThoai() : "",
                k.getEmail() != null ? k.getEmail() : "",
                k.getDiemTichLuy() != null ? k.getDiemTichLuy() : 0
            );
        }
    }

    record VoucherDTO(Long id, String maPhieu, String tenPhieu,
                      String hinhThucGiam, BigDecimal giaTriGiam,
                      BigDecimal giaTriToiThieu, Integer soLuong) {
        static VoucherDTO from(PhieuGiamGia p) {
            return new VoucherDTO(
                p.getId(), p.getMaPhieu(), p.getTenPhieu(),
                p.getHinhThucGiam(), p.getGiaTriGiam(),
                p.getGiaTriToiThieu(), p.getSoLuong()
            );
        }
    }

    record QuickCreateKhRequest(String tenDayDu, String soDienThoai, String email) {}

    record CheckoutRequest(BigDecimal tienKhachDua, String phuongThuc, String loaiDon) {}
}