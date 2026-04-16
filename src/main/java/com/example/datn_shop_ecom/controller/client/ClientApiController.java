package com.example.datn_shop_ecom.controller.client;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import com.example.datn_shop_ecom.service.KhachHangService;
import com.example.datn_shop_ecom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import com.example.datn_shop_ecom.service.EmailService;
import com.example.datn_shop_ecom.service.GioHangService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
public class ClientApiController {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SanPhamChiTietRepository spctRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @GetMapping("/cart/items/{khId}")
    public ResponseEntity<?> getCartItems(@PathVariable Long khId) {
        return ResponseEntity.ok(gioHangService.getCartItems(khId).stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct == null) return null;

            map.put("spctId", spct.getId());
            map.put("ten", spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm lỗi");
            
            String anh = spct.getDuongDanAnh();
            if (anh == null && spct.getSanPham() != null) {
                anh = spct.getSanPham().getDuongDanAnh();
            }
            map.put("anh", anh);
            map.put("gia", spct.getGiaBan());
            map.put("mauSac", spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "N/A");
            map.put("kichThuoc", spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "N/A");
            map.put("soLuong", ct.getSoLuong());
            return map;
        }).filter(Objects::nonNull).toList());
    }

    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCartDb(@RequestBody Map<String, Object> body) {
        try {
            Long khId = body.get("khId") != null ? Long.valueOf(body.get("khId").toString()) : null;
            Long spctId = body.get("spctId") != null ? Long.valueOf(body.get("spctId").toString()) : null;
            int qty = body.get("soLuong") != null ? Integer.parseInt(body.get("soLuong").toString()) : 1;
            
            if (khId != null && spctId != null) {
                gioHangService.addToCart(khId, spctId, qty);
                return ResponseEntity.ok(Map.of("success", true));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing parameters"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/cart/sync")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> syncCart(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("khId") == null) return ResponseEntity.badRequest().body("khId required");
            Long khId = Long.valueOf(body.get("khId").toString());
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null) {
                gioHangService.syncCart(khId, items);
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/cart/remove")
    public ResponseEntity<?> removeFromCartDb(@RequestBody Map<String, Object> body) {
        Long khId = Long.valueOf(body.get("khId").toString());
        Long spctId = Long.valueOf(body.get("spctId").toString());
        gioHangService.removeItem(khId, spctId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Map<String, Object> resp = new HashMap<>();

        Optional<KhachHang> opt = khachHangRepository.findByEmail(email);
        if (opt.isEmpty() || !opt.get().getMatKhau().equals(password)) {
            resp.put("success", false);
            resp.put("message", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.ok(resp);
        }

        KhachHang kh = opt.get();
        String token = tokenProvider.generateToken(new UsernamePasswordAuthenticationToken(email, password));

        Map<String, Object> user = new HashMap<>();
        user.put("id", kh.getId());
        user.put("hoTen", kh.getTenDayDu());
        user.put("email", kh.getEmail());
        user.put("soDienThoai", kh.getSoDienThoai());
        user.put("gioiTinh", kh.getGioiTinh());
        user.put("ngaySinh", kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : null);
        user.put("token", token);

        resp.put("success", true);
        resp.put("user", user);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/vouchers/check")
    public ResponseEntity<?> checkVoucher(@RequestParam String code, @RequestParam Double cartTotal, @RequestParam(required = false) Long khId) {
        Map<String, Object> resp = new HashMap<>();
        Optional<PhieuGiamGia> opt = phieuGiamGiaRepository.findByMaPhieuAndXoaMemFalse(code);

        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Mã giảm giá không tồn tại!");
            return ResponseEntity.ok(resp);
        }

        PhieuGiamGia voucher = opt.get();
        LocalDate now = LocalDate.now();

        if (voucher.getNgayBatDau() != null && voucher.getNgayBatDau().isAfter(now)) {
            resp.put("success", false);
            resp.put("message", "Mã giảm giá chưa đến thời gian sử dụng!");
            return ResponseEntity.ok(resp);
        }
        if (voucher.getNgayKetThuc() != null && voucher.getNgayKetThuc().isBefore(now)) {
            resp.put("success", false);
            resp.put("message", "Mã giảm giá đã hết hạn!");
            return ResponseEntity.ok(resp);
        }
        if (voucher.getSoLuong() != null && voucher.getSoLuong() <= 0) {
            resp.put("success", false);
            resp.put("message", "Mã giảm giá đã hết lượt sử dụng!");
            return ResponseEntity.ok(resp);
        }
        if (voucher.getGiaTriToiThieu() != null && cartTotal < voucher.getGiaTriToiThieu().doubleValue()) {
            resp.put("success", false);
            resp.put("message", "Đơn hàng tối thiểu " + String.format("%,.0f đ", voucher.getGiaTriToiThieu()) + " mới có thể sử dụng mã này!");
            return ResponseEntity.ok(resp);
        }

        if (khId != null && hoaDonRepository.existsByKhachHangIdAndPhieuGiamGiaId(khId, voucher.getId())) {
            resp.put("success", false);
            resp.put("message", "Bạn đã sử dụng mã giảm giá này cho đơn hàng khác rồi!");
            return ResponseEntity.ok(resp);
        }

        resp.put("success", true);
        resp.put("id", voucher.getId());
        resp.put("ma", voucher.getMaPhieu());
        resp.put("hinhThuc", voucher.getHinhThucGiam());
        resp.put("giaTri", voucher.getGiaTriGiam());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/vouchers/used/{khId}")
    public ResponseEntity<?> getUsedVouchers(@PathVariable Long khId) {
        return ResponseEntity.ok(hoaDonRepository.findUsedVoucherIdsByKhachHangId(khId));
    }

    @PostMapping("/validate-cart")
    public ResponseEntity<?> validateCart(@RequestBody List<Map<String, Object>> items) {
        Map<String, Object> resp = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Long spctId = Long.valueOf(item.get("spctId").toString());
            int qtyRequested = Integer.parseInt(item.get("soLuong").toString());

            Optional<SanPhamChiTiet> opt = spctRepository.findById(spctId);
            if (opt.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("tenSanPham", "Sản phẩm không tồn tại");
                err.put("soLuongYeuCau", qtyRequested);
                err.put("soTonKho", 0);
                errors.add(err);
            } else {
                SanPhamChiTiet spct = opt.get();
                if (spct.getSoTonKho() < qtyRequested) {
                    Map<String, Object> err = new HashMap<>();
                    String name = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "SẢN PHẨM";
                    String ms = spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : "";
                    String kt = spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : "";
                    
                    err.put("tenSanPham", name + " [" + ms + " - " + kt + "]");
                    err.put("soLuongYeuCau", qtyRequested);
                    err.put("soTonKho", spct.getSoTonKho());
                    errors.add(err);
                }
            }
        }

        if (!errors.isEmpty()) {
            resp.put("success", false);
            resp.put("errors", errors);
        } else {
            resp.put("success", true);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            KhachHang kh = new KhachHang();
            kh.setTenDayDu((String) body.get("tenDayDu"));
            kh.setEmail((String) body.get("email"));
            kh.setSoDienThoai((String) body.get("soDienThoai"));
            kh.setGioiTinh((String) body.get("gioiTinh"));
            String ngaySinhStr = (String) body.get("ngaySinh");
            if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                kh.setNgaySinh(java.time.LocalDate.parse(ngaySinhStr));
            }
            
            // Map địa chỉ
            java.util.List<Map<String, String>> addrList = (java.util.List<Map<String, String>>) body.get("addresses");
            if (addrList != null) {
                for (Map<String, String> addrMap : addrList) {
                    com.example.datn_shop_ecom.entity.DiaChi dc = new com.example.datn_shop_ecom.entity.DiaChi();
                    dc.setTenNguoiNhan(addrMap.get("tenNguoiNhan"));
                    dc.setTinhThanhPho(addrMap.get("tinhThanhPho"));
                    dc.setQuanHuyen(addrMap.get("quanHuyen"));
                    dc.setXaPhuong(addrMap.get("xaPhuong"));
                    dc.setChiTiet(addrMap.get("chiTiet"));
                    dc.setDiaChiMacDinh(false);
                    kh.getDanhSachDiaChi().add(dc);
                }
                if (!kh.getDanhSachDiaChi().isEmpty()) {
                    kh.getDanhSachDiaChi().get(0).setDiaChiMacDinh(true);
                }
            }
            
            khachHangService.registerKhachHang(kh);
            resp.put("success", true);
            resp.put("message", "Đăng ký thành công! Mật khẩu đã gửi vào email.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.ok(resp);
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Long id = Long.valueOf(body.get("id").toString());
            Optional<KhachHang> opt = khachHangRepository.findById(id);
            if (opt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Không tìm thấy khách hàng");
                return ResponseEntity.ok(resp);
            }
            KhachHang kh = opt.get();
            kh.setTenDayDu((String) body.get("hoTen"));
            kh.setSoDienThoai((String) body.get("soDienThoai"));
            kh.setGioiTinh((String) body.get("gioiTinh"));
            String ngaySinhStr = (String) body.get("ngaySinh");
            if (ngaySinhStr != null && !ngaySinhStr.isEmpty()) {
                kh.setNgaySinh(LocalDate.parse(ngaySinhStr));
            }
            khachHangRepository.save(kh);
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", kh.getId());
            user.put("hoTen", kh.getTenDayDu());
            user.put("email", kh.getEmail());
            user.put("soDienThoai", kh.getSoDienThoai());
            user.put("gioiTinh", kh.getGioiTinh());
            user.put("ngaySinh", kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : null);
            user.put("token", body.get("token"));
            resp.put("success", true);
            resp.put("user", user);
            resp.put("message", "Cập nhật thành công!");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.ok(resp);
        }
    }

    @GetMapping("/provinces")
    public ResponseEntity<String> getProvinces() {
        return callExternal("https://provinces.open-api.vn/api/p/");
    }

    @GetMapping("/districts/{code}")
    public ResponseEntity<String> getDistricts(@PathVariable String code) {
        return callExternal("https://provinces.open-api.vn/api/p/" + code + "?depth=2");
    }

    @GetMapping("/wards/{code}")
    public ResponseEntity<String> getWards(@PathVariable String code) {
        return callExternal("https://provinces.open-api.vn/api/d/" + code + "?depth=2");
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        return khachHangRepository.findById(id)
            .map(kh -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", kh.getId());
                map.put("tenDayDu", kh.getTenDayDu());
                map.put("email", kh.getEmail());
                map.put("soDienThoai", kh.getSoDienThoai());
                List<Map<String, Object>> addrs = kh.getDanhSachDiaChi().stream().map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("tenNguoiNhan", a.getTenNguoiNhan());
                    m.put("soDienThoaiNguoiNhan", a.getSoDienThoaiNguoiNhan());
                    m.put("tinhThanhPho", a.getTinhThanhPho());
                    m.put("quanHuyen", a.getQuanHuyen());
                    m.put("xaPhuong", a.getXaPhuong());
                    m.put("chiTiet", a.getChiTiet());
                    m.put("diaChiMacDinh", a.getDiaChiMacDinh());
                    return m;
                }).toList();
                map.put("danhSachDiaChi", addrs);
                return ResponseEntity.ok(map);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/checkout")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("khachHangId") == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ID khách hàng"));
            
            Long khId = Long.valueOf(body.get("khachHangId").toString());
            String ten = (String) body.get("tenNguoiNhan");
            String sdt = (String) body.get("soDienThoai");
            String ttp = (String) body.get("tinhThanhPho");
            String qh = (String) body.get("quanHuyen");
            String xp = (String) body.get("xaPhuong");
            String ct = (String) body.get("chiTiet");
            Double phiShip = body.get("phiShip") != null ? Double.valueOf(body.get("phiShip").toString()) : 0.0;
            Long voucherId = body.get("idPhieuGiamGia") != null && !body.get("idPhieuGiamGia").toString().isEmpty() ? Long.valueOf(body.get("idPhieuGiamGia").toString()) : null;
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("cartItems");

            // Calculate subtotal from items to ensure accuracy
            java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
            for (Map<String, Object> item : items) {
                Long spctId = Long.valueOf(item.get("spctId").toString());
                int qty = Integer.parseInt(item.get("soLuong").toString());
                SanPhamChiTiet spct = spctRepository.findById(spctId).orElse(null);
                if (spct != null) {
                    subTotal = subTotal.add(spct.getGiaBan().multiply(java.math.BigDecimal.valueOf(qty)));
                }
            }

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-" + System.currentTimeMillis());
            hd.setKhachHang(khachHangRepository.findById(khId).orElse(null));
            hd.setTenNguoiNhan(ten);
            hd.setSoDienThoaiNguoiNhan(sdt);
            hd.setTinhThanhPho(ttp);
            hd.setQuanHuyen(qh);
            hd.setXaPhuong(xp);
            hd.setChiTietNguoiNhan(ct);
            hd.setTongTien(subTotal);
            hd.setTienVanChuyen(java.math.BigDecimal.valueOf(phiShip));
            hd.setNgayDatHang(java.time.LocalDateTime.now());
            hd.setNgayTao(java.time.LocalDateTime.now());
            hd.setTrangThaiHoaDon("CHO_XAC_NHAN");
            hd.setLoaiHoaDon("Giao hàng");
            hd.setPhuongThucThanhToan("Thanh toán bằng tiền mặt");
            
            java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
            // Xử lý Voucher trước khi lưu hóa đơn
            if (voucherId != null) {
                // 1. Kiểm tra xem khách hàng đã dùng mã này chưa
                if (hoaDonRepository.existsByKhachHangIdAndPhieuGiamGiaId(khId, voucherId)) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Bạn đã sử dụng mã giảm giá này cho đơn hàng trước đó rồi."));
                }

                Optional<PhieuGiamGia> vOpt = phieuGiamGiaRepository.findById(voucherId);
                if (vOpt.isPresent()) {
                    PhieuGiamGia v = vOpt.get();
                    hd.setPhieuGiamGia(v); // Gán thực thể voucher vào hóa đơn

                    // 2. Kiểm tra số lượng lượt dùng
                    if (v.getSoLuong() != null && v.getSoLuong() <= 0) {
                        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã giảm giá này đã hết lượt sử dụng."));
                    }
                    
                    // Tính toán mức giảm
                    if ("VNĐ".equals(v.getHinhThucGiam())) {
                        discountAmount = v.getGiaTriGiam();
                    } else { // %
                        discountAmount = subTotal.multiply(v.getGiaTriGiam()).divide(java.math.BigDecimal.valueOf(100));
                    }
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;
                    
                    // 3. Giảm số lượng
                    if (v.getSoLuong() != null) {
                        v.setSoLuong(v.getSoLuong() - 1);
                        phieuGiamGiaRepository.save(v);
                    }
                }
            }
            
            hd.setTienPhieuGiamGia(discountAmount);
            hd.setTongTienAfterGiam(subTotal.add(hd.getTienVanChuyen()).subtract(discountAmount));

            HoaDon savedHd = hoaDonRepository.save(hd);

            for (Map<String, Object> item : items) {
                Long spctId = Long.valueOf(item.get("spctId").toString());
                int qty = Integer.parseInt(item.get("soLuong").toString());
                SanPhamChiTiet spct = spctRepository.findById(spctId).orElse(null);
                if (spct != null) {
                    ChiTietHoaDon cthd = new ChiTietHoaDon();
                    cthd.setHoaDon(savedHd);
                    cthd.setSanPhamChiTiet(spct);
                    cthd.setSoLuong(qty);
                    cthd.setGia(spct.getGiaBan());
                    cthd.setNgayTao(java.time.LocalDateTime.now());
                    chiTietHoaDonRepository.save(cthd);
                }
            }

            for (Map<String, Object> item : items) {
                Long spctId = Long.valueOf(item.get("spctId").toString());
                gioHangService.removeItem(khId, spctId);
            }

            try {
                KhachHang kh = savedHd.getKhachHang();
                if (kh != null && kh.getEmail() != null) {
                    String subject = "Xác nhận đơn hàng thành công - PeakSneaker " + savedHd.getMaHoaDon();
                    String emailBody = "Xin chào " + savedHd.getTenNguoiNhan() + ",\n\n" +
                                 "Chúc mừng bạn đã đặt hàng thành công tại PeakSneaker!\n" +
                                 "Mã đơn hàng của bạn là: " + savedHd.getMaHoaDon() + "\n" +
                                 "Tổng tiền thanh toán: " + String.format("%,.0f đ", savedHd.getTongTienAfterGiam()) + "\n\n" +
                                 "Chúng tôi sẽ sớm liên hệ để xác nhận và giao hàng cho bạn.\n" +
                                 "Cảm ơn bạn đã tin tưởng và mua sắm tại PeakSneaker!";
                    emailService.sendEmail(kh.getEmail(), subject, emailBody);
                }
            } catch (Exception e) {}

            return ResponseEntity.ok(Map.of("success", true, "maHoaDon", savedHd.getMaHoaDon()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private ResponseEntity<String> callExternal(String urlStr) {
        try {
            URL url = java.net.URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line; StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) sb.append(line);
            in.close();
            return ResponseEntity.ok(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error");
        }
    }

    @GetMapping("/tra-cuu")
    public ResponseEntity<?> traCuuDonHang(@RequestParam("maHoaDon") String maHoaDon) {
        Map<String, Object> resp = new HashMap<>();
        Optional<HoaDon> opt = hoaDonRepository.findByMaHoaDon(maHoaDon.trim());
        if (opt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Không tìm thấy đơn hàng mã: " + maHoaDon);
            return ResponseEntity.ok(resp);
        }
        HoaDon hd = opt.get();
        Map<String, Object> hDon = new HashMap<>();
        hDon.put("maHoaDon", hd.getMaHoaDon());
        hDon.put("ngayDatHang", hd.getNgayDatHang() != null ? hd.getNgayDatHang() : hd.getNgayTao());
        hDon.put("tenNguoiNhan", hd.getTenNguoiNhan());
        hDon.put("soDienThoaiNguoiNhan", hd.getSoDienThoaiNguoiNhan());
        String dchi = "";
        if (hd.getChiTietNguoiNhan() != null && !hd.getChiTietNguoiNhan().isEmpty()) dchi += hd.getChiTietNguoiNhan() + ", ";
        if (hd.getXaPhuong() != null && !hd.getXaPhuong().isEmpty()) dchi += hd.getXaPhuong() + ", ";
        if (hd.getQuanHuyen() != null && !hd.getQuanHuyen().isEmpty()) dchi += hd.getQuanHuyen() + ", ";
        if (hd.getTinhThanhPho() != null && !hd.getTinhThanhPho().isEmpty()) dchi += hd.getTinhThanhPho();
        if (dchi.endsWith(", ")) dchi = dchi.substring(0, dchi.length() - 2);
        hDon.put("diaChiGiao", dchi);
        hDon.put("tongTien", hd.getTongTien());
        hDon.put("tienVanChuyen", hd.getTienVanChuyen());
        hDon.put("tienPhieuGiamGia", hd.getTienPhieuGiamGia());
        hDon.put("tongTienAfterGiam", hd.getTongTienAfterGiam());
        hDon.put("ghiChu", hd.getMoTa());
        hDon.put("trangThai", hd.getTrangThaiHoaDon());

        List<ChiTietHoaDon> chiTiets = chiTietHoaDonRepository.findByHoaDonId(hd.getId());
        List<Map<String, Object>> spList = new ArrayList<>();
        for (ChiTietHoaDon ct : chiTiets) {
            Map<String, Object> spMap = new HashMap<>();
            if (ct.getSanPhamChiTiet() != null) {
                if (ct.getSanPhamChiTiet().getSanPham() != null) {
                    spMap.put("tenSanPham", ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
                } else {
                    spMap.put("tenSanPham", "Sản phẩm không rõ");
                }
                
                String anh = ct.getSanPhamChiTiet().getDuongDanAnh();
                // Nếu biến thể không có ảnh, lấy ảnh của sản phẩm chính làm dự phòng
                if (anh == null || anh.trim().isEmpty()) {
                    if (ct.getSanPhamChiTiet().getSanPham() != null) {
                        anh = ct.getSanPhamChiTiet().getSanPham().getDuongDanAnh();
                    }
                }
                spMap.put("duongDanAnh", anh);
                
                spMap.put("mauSac", ct.getSanPhamChiTiet().getMauSac() != null ? ct.getSanPhamChiTiet().getMauSac().getTenMauSac() : "");
                spMap.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc() != null ? ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc() : "");
            } else {
                spMap.put("tenSanPham", "Sản phẩm không rõ");
            }
            spMap.put("soLuong", ct.getSoLuong());
            spMap.put("gia", ct.getGia());
            spList.add(spMap);
        }
        hDon.put("chiTietSanPham", spList);
        resp.put("success", true);
        resp.put("hoaDon", hDon);
        return ResponseEntity.ok(resp);
    }
}