package com.example.datn_shop_ecom.controller.client;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import com.example.datn_shop_ecom.service.KhachHangService;
import com.example.datn_shop_ecom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.datn_shop_ecom.service.GioHangService;
import com.example.datn_shop_ecom.service.EmailService;
import com.example.datn_shop_ecom.service.GHNService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private SanPhamChiTietRepository spctRepository;

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private GHNService ghnService;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("clientAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @GetMapping("/auth/get-token")
    public ResponseEntity<?> getToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String token = tokenProvider.generateToken(authentication);
            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("username", authentication.getName());
            resp.put("roles", authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toList()));
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }

    @GetMapping("/cart/items/{khId}")
    public ResponseEntity<?> getCartItems(@PathVariable Long khId) {
        return ResponseEntity.ok(gioHangService.getCartItems(khId).stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            map.put("id", ct.getId());
            map.put("spctId", spct.getId());
            map.put("tenSanPham", spct.getSanPham().getTenSanPham());
            map.put("mauSac", spct.getMauSac().getTenMauSac());
            map.put("kichThuoc", spct.getKichThuoc().getTenKichThuoc());
            map.put("soLuong", ct.getSoLuong());
            map.put("gia", spct.getGiaBan());
            map.put("anh", spct.getDuongDanAnh());
            map.put("soLuongTon", spct.getSoTonKho());
            return map;
        }).toList());
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
            if (body.get("khId") == null)
                return ResponseEntity.badRequest().body("khId required");
            Long khId = Long.valueOf(body.get("khId").toString());
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items != null)
                gioHangService.syncCart(khId, items);
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email") != null ? body.get("email").trim() : "";
        String password = body.get("password");
        Map<String, Object> resp = new HashMap<>();
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            Optional<KhachHang> khOpt = khachHangRepository.findByEmail(email);
            String token = tokenProvider.generateToken(authentication);
            Map<String, Object> user = new HashMap<>();
            if (khOpt.isPresent()) {
                KhachHang kh = khOpt.get();
                user.put("id", kh.getId());
                user.put("hoTen", kh.getTenDayDu());
                user.put("email", kh.getEmail());
                user.put("soDienThoai", kh.getSoDienThoai());
                user.put("gioiTinh", kh.getGioiTinh());
                user.put("ngaySinh", kh.getNgaySinh());
            } else {
                user.put("id", 0);
                user.put("hoTen", authentication.getName());
                user.put("email", email);
                user.put("soDienThoai", "");
            }
            user.put("token", token);
            resp.put("success", true);
            resp.put("user", user);
            return ResponseEntity.ok(resp);

        } catch (org.springframework.security.authentication.InternalAuthenticationServiceException e) {
            resp.put("success", false);
            resp.put("message", "Email của bạn chưa được đăng ký trên hệ thống!");
            return ResponseEntity.status(401).body(resp);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            resp.put("success", false);
            resp.put("message", "Mật khẩu bạn nhập không chính xác!");
            return ResponseEntity.status(401).body(resp);
        } catch (org.springframework.security.authentication.DisabledException e) {
            resp.put("success", false);
            resp.put("message", "Tài khoản của bạn đã bị ngừng hoạt động!");
            return ResponseEntity.status(403).body(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String hoTen = (String) body.get("tenDayDu");
            String sdt = (String) body.get("soDienThoai");
            if (hoTen == null || !hoTen.matches("^[\\p{L}\\s]+$")) {
                resp.put("success", false);
                resp.put("message", "Họ tên không được chứa số và ký tự đặc biệt!");
                return ResponseEntity.status(400).body(resp);
            }
            if (sdt == null || !sdt.matches("^0\\d{9}$")) {
                resp.put("success", false);
                resp.put("message", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số!");
                return ResponseEntity.status(400).body(resp);
            }
            KhachHang kh = new KhachHang();
            kh.setTenDayDu(hoTen);
            kh.setEmail((String) body.get("email"));
            kh.setSoDienThoai(sdt);
            kh.setGioiTinh((String) body.get("gioiTinh"));
            if (body.get("ngaySinh") != null && !body.get("ngaySinh").toString().isEmpty())
                kh.setNgaySinh(LocalDate.parse(body.get("ngaySinh").toString()));
            khachHangService.registerKhachHang(kh);
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Đăng ký thất bại: " + e.getMessage());
            return ResponseEntity.status(400).body(resp);
        }
    }

    @PostMapping("/re-auth")
    public ResponseEntity<?> reAuth(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        Optional<KhachHang> opt = khachHangRepository.findByEmail(email);
        if (opt.isPresent()) {
            KhachHang kh = opt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(kh.getEmail(), null,
                    List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.status(401).body(Map.of("success", false));
    }

    @GetMapping("/vouchers/best")
    public ResponseEntity<?> getBestVoucher(@RequestParam Double cartTotal, @RequestParam(required = false) Long khId) {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaRepository.findAllByXoaMemFalse();
            LocalDate now = LocalDate.now();
            List<Long> usedVoucherIds = (khId != null) ? hoaDonRepository.findUsedVoucherIdsByKhachHangId(khId)
                    : new ArrayList<>();
            Optional<PhieuGiamGia> best = list.stream()
                    .filter(v -> !usedVoucherIds.contains(v.getId()))
                    .filter(v -> v.getNgayBatDau() == null || !v.getNgayBatDau().isAfter(now))
                    .filter(v -> v.getNgayKetThuc() == null || !v.getNgayKetThuc().isBefore(now))
                    .filter(v -> v.getTrangThai() != null && v.getTrangThai() == 1)
                    .filter(v -> v.getSoLuong() == null || v.getSoLuong() > 0)
                    .filter(v -> v.getGiaTriToiThieu() != null && v.getGiaTriToiThieu().doubleValue() <= cartTotal)
                    .max(Comparator.comparing(v -> "VNĐ".equals(v.getHinhThucGiam()) ? v.getGiaTriGiam().doubleValue()
                            : cartTotal * v.getGiaTriGiam().doubleValue() / 100.0));
            if (best.isPresent()) {
                PhieuGiamGia v = best.get();
                return ResponseEntity.ok(Map.of("success", true, "id", v.getId(), "ma", v.getMaPhieu(), "hinhThuc",
                        v.getHinhThucGiam(), "giaTri", v.getGiaTriGiam()));
            }
            return ResponseEntity.ok(Map.of("success", false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/validate-cart")
    public ResponseEntity<?> validateCart(@RequestBody List<Map<String, Object>> items) {
        List<Map<String, Object>> errors = new ArrayList<>();
        boolean success = true;
        for (Map<String, Object> item : items) {
            Long spctId = Long.valueOf(item.get("spctId").toString());
            int qty = Integer.parseInt(item.get("soLuong").toString());
            SanPhamChiTiet spct = spctRepository.findById(spctId).orElse(null);
            if (spct == null || spct.getSoTonKho() < qty) {
                success = false;
                errors.add(
                        Map.of("spctId", spctId, "tenSanPham", spct != null ? spct.getSanPham().getTenSanPham() : "N/A",
                                "soLuongTon", spct != null ? spct.getSoTonKho() : 0));
            }
        }
        return ResponseEntity.ok(Map.of("success", success, "errors", errors));
    }

    @PostMapping("/checkout")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body) {
        try {
            Long khId = body.get("khachHangId") != null ? Long.valueOf(body.get("khachHangId").toString()) : null;
            String ten = (String) body.get("tenNguoiNhan");
            String sdt = (String) body.get("soDienThoai");
            
            if (ten == null || !ten.matches("^[\\p{L}\\s]+$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Họ tên không được chứa số và ký tự đặc biệt!"));
            }
            if (sdt == null || !sdt.matches("^0\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số!"));
            }

            String ttp = (String) body.get("tinhThanhPho");
            String qh = (String) body.get("quanHuyen");
            String xp = (String) body.get("xaPhuong");
            String ct = (String) body.get("chiTiet");
            double phiShip = body.get("phiShip") != null ? Double.parseDouble(body.get("phiShip").toString()) : 0;
            Long voucherId = body.get("idPhieuGiamGia") != null ? Long.valueOf(body.get("idPhieuGiamGia").toString())
                    : null;
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) body.get("cartItems");

            java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
            for (Map<String, Object> item : cartItems) {
                SanPhamChiTiet spct = spctRepository.findById(Long.valueOf(item.get("spctId").toString())).orElse(null);
                if (spct != null)
                    subTotal = subTotal.add(spct.getGiaBan()
                            .multiply(java.math.BigDecimal.valueOf(Integer.parseInt(item.get("soLuong").toString()))));
            }

            HoaDon hd = new HoaDon();
            hd.setMaHoaDon("HD-" + System.currentTimeMillis());
            if (khId != null)
                hd.setKhachHang(khachHangRepository.findById(khId).orElse(null));
            hd.setTenNguoiNhan(ten);
            hd.setSoDienThoaiNguoiNhan(sdt);
            hd.setTinhThanhPho(ttp);
            hd.setQuanHuyen(qh);
            hd.setXaPhuong(xp);
            hd.setChiTietNguoiNhan(ct);
            hd.setTongTien(subTotal);
            hd.setTienVanChuyen(java.math.BigDecimal.valueOf(phiShip));
            hd.setTrangThaiHoaDon("CHO_XAC_NHAN");
            hd.setLoaiHoaDon("Giao hàng");
            hd.setNgayTao(LocalDateTime.now());
            hd.setPhuongThucThanhToan("Thanh toán bằng tiền mặt");
            hd.setEmailNguoiNhan((String) body.get("email"));

            java.math.BigDecimal discount = java.math.BigDecimal.ZERO;
            if (voucherId != null) {
                PhieuGiamGia v = phieuGiamGiaRepository.findById(voucherId).orElse(null);
                if (v != null) {
                    hd.setPhieuGiamGia(v);
                    discount = "VNĐ".equals(v.getHinhThucGiam()) ? v.getGiaTriGiam()
                            : subTotal.multiply(v.getGiaTriGiam()).divide(java.math.BigDecimal.valueOf(100));
                    v.setSoLuong(v.getSoLuong() - 1);
                    phieuGiamGiaRepository.save(v);
                }
            }
            hd.setTienPhieuGiamGia(discount);
            hd.setTongTienAfterGiam(subTotal.add(hd.getTienVanChuyen()).subtract(discount));
            HoaDon savedHd = hoaDonRepository.save(hd);

            // TỰ ĐỘNG LƯU ĐỊA CHỈ MỚI CHO KHÁCH HÀNG (Nếu chưa có)
            if (khId != null) {
                Optional<DiaChi> exstDiaChi = diaChiRepository
                        .findByKhachHangIdAndTenNguoiNhanAndSoDienThoaiNguoiNhanAndTinhThanhPhoAndQuanHuyenAndXaPhuongAndChiTiet(
                                khId, ten, sdt, ttp, qh, xp, ct);
                if (exstDiaChi.isEmpty()) {
                    DiaChi newDc = new DiaChi();
                    newDc.setKhachHang(savedHd.getKhachHang());
                    newDc.setTenNguoiNhan(ten);
                    newDc.setSoDienThoaiNguoiNhan(sdt);
                    newDc.setTinhThanhPho(ttp);
                    newDc.setQuanHuyen(qh);
                    newDc.setXaPhuong(xp);
                    newDc.setChiTiet(ct);
                    newDc.setNgayTao(LocalDateTime.now());
                    newDc.setDiaChiMacDinh(false);
                    newDc.setXoaMem(false);
                    diaChiRepository.save(newDc);
                }
            }

            for (Map<String, Object> item : cartItems) {
                SanPhamChiTiet spct = spctRepository.findById(Long.valueOf(item.get("spctId").toString())).orElse(null);
                if (spct != null) {
                    ChiTietHoaDon cthd = new ChiTietHoaDon();
                    cthd.setHoaDon(savedHd);
                    cthd.setSanPhamChiTiet(spct);
                    cthd.setSoLuong(Integer.parseInt(item.get("soLuong").toString()));
                    cthd.setGia(spct.getGiaBan());
                    cthd.setThanhTien(spct.getGiaBan().multiply(java.math.BigDecimal.valueOf(cthd.getSoLuong())));
                    chiTietHoaDonRepository.save(cthd);
                    // ĐÃ XÓA: spct.setSoTonKho(spct.getSoTonKho() - cthd.getSoLuong());
                    // ĐÃ XÓA: spctRepository.save(spct);
                }
            }
            if (khId != null)
                gioHangService.clearCart(khId);

            // --- GỬI EMAIL THÔNG BÁO ---
            try {
                String emailNhan = (String) body.get("email");
                if (emailNhan != null && !emailNhan.isEmpty()) {
                    String subject = "Xác nhận đơn hàng thành công - PeakSneaker " + savedHd.getMaHoaDon();
                    String bodyMail = String.format(
                            "Chào %s,\n\nĐơn hàng %s của bạn đã được hệ thống tiếp nhận thành công.\n" +
                                    "Tổng số tiền thanh toán: %s VNĐ.\n" +
                                    "Địa chỉ nhận hàng: %s, %s, %s, %s.\n\n" +
                                    "Cảm ơn bạn đã tin tưởng và mua sắm tại PeakSneaker!",
                            ten, savedHd.getMaHoaDon(), savedHd.getTongTienAfterGiam(), ct, xp, qh, ttp);
                    emailService.sendEmail(emailNhan, subject, bodyMail);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("success", true, "maHoaDon", savedHd.getMaHoaDon()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/profile/{khId}")
    public ResponseEntity<?> getProfile(@PathVariable Long khId) {
        try {
            KhachHang kh = khachHangRepository.findById(khId).orElse(null);
            if (kh == null)
                return ResponseEntity.notFound().build();
            Map<String, Object> map = new HashMap<>();
            map.put("id", kh.getId());
            map.put("tenDayDu", kh.getTenDayDu());
            map.put("email", kh.getEmail());
            map.put("soDienThoai", kh.getSoDienThoai());
            map.put("gioiTinh", kh.getGioiTinh());
            map.put("ngaySinh", kh.getNgaySinh());
            map.put("danhSachDiaChi",
                    diaChiRepository.findByKhachHangId(khId).stream().filter(d -> !d.getXoaMem()).toList());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        try {
            String hoTen = (String) body.get("hoTen");
            String sdt = (String) body.get("soDienThoai");
            if (hoTen == null || !hoTen.matches("^[\\p{L}\\s]+$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Họ tên không được chứa số và ký tự đặc biệt!"));
            }
            if (sdt == null || !sdt.matches("^0\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số!"));
            }

            KhachHang kh = khachHangRepository.findById(Long.valueOf(body.get("id").toString())).orElse(null);
            if (kh == null)
                return ResponseEntity.notFound().build();
            kh.setTenDayDu(hoTen);
            kh.setSoDienThoai(sdt);
            kh.setGioiTinh((String) body.get("gioiTinh"));
            if (body.get("ngaySinh") != null && !body.get("ngaySinh").toString().isEmpty())
                kh.setNgaySinh(LocalDate.parse(body.get("ngaySinh").toString()));
            khachHangRepository.save(kh);

            Map<String, Object> user = new HashMap<>();
            user.put("id", kh.getId());
            user.put("hoTen", kh.getTenDayDu());
            user.put("email", kh.getEmail());
            user.put("soDienThoai", kh.getSoDienThoai());
            user.put("gioiTinh", kh.getGioiTinh());
            user.put("ngaySinh", kh.getNgaySinh());
            user.put("token", body.get("token"));

            return ResponseEntity.ok(Map.of("success", true, "user", user));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/orders/{khId}")
    public ResponseEntity<?> getOrdersByKhachHang(@PathVariable Long khId) {
        return ResponseEntity.ok(hoaDonRepository.findByKhachHangIdOrderByNgayTaoDesc(khId).stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getId());
            m.put("maHoaDon", h.getMaHoaDon());
            m.put("ngayTao", h.getNgayTao());
            m.put("tongTienAfterGiam", h.getTongTienAfterGiam());
            m.put("trangThai", h.getTrangThaiHoaDon());
            List<ChiTietHoaDon> details = chiTietHoaDonRepository.findByHoaDonId(h.getId());
            if (!details.isEmpty()) {
                m.put("firstItemName", details.get(0).getSanPhamChiTiet().getSanPham().getTenSanPham());
                String img = details.get(0).getSanPhamChiTiet().getDuongDanAnh();
                if (img == null || img.isEmpty())
                    img = details.get(0).getSanPhamChiTiet().getSanPham().getDuongDanAnh();
                m.put("firstItemImage", img);
                m.put("totalItems", details.stream().mapToInt(ChiTietHoaDon::getSoLuong).sum());
                m.put("otherItemsCount", details.size() - 1);
            }
            return m;
        }).toList());
    }

    @GetMapping("/orders/detail/{orderId}")
    public ResponseEntity<?> getOrderDetailByClient(@PathVariable Long orderId) {
        HoaDon hd = hoaDonRepository.findById(orderId).orElse(null);
        if (hd == null)
            return ResponseEntity.notFound().build();
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", hd.getId());
        resp.put("maHoaDon", hd.getMaHoaDon());
        resp.put("ngayTao", hd.getNgayTao());
        resp.put("tenNguoiNhan", hd.getTenNguoiNhan());
        resp.put("soDienThoai", hd.getSoDienThoaiNguoiNhan());
        resp.put("email", hd.getEmailNguoiNhan());
        resp.put("diaChi", String.format("%s, %s, %s, %s", hd.getChiTietNguoiNhan(), hd.getXaPhuong(),
                hd.getQuanHuyen(), hd.getTinhThanhPho()));
        resp.put("tongTien", hd.getTongTien());
        resp.put("phiShip", hd.getTienVanChuyen());
        resp.put("giamGia", hd.getTienPhieuGiamGia());
        resp.put("thanhTien", hd.getTongTienAfterGiam());
        resp.put("trangThai", hd.getTrangThaiHoaDon());
        resp.put("phuongThuc", hd.getPhuongThucThanhToan());
        resp.put("ghiChu", hd.getMoTa());
        resp.put("items", chiTietHoaDonRepository.findByHoaDonId(orderId).stream().map(ct -> {
            Map<String, Object> m = new HashMap<>();
            m.put("tenSanPham", ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
            m.put("mauSac", ct.getSanPhamChiTiet().getMauSac().getTenMauSac());
            m.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
            m.put("soLuong", ct.getSoLuong());
            m.put("gia", ct.getGia());
            String img = ct.getSanPhamChiTiet().getDuongDanAnh();
            if (img == null || img.isEmpty())
                img = ct.getSanPhamChiTiet().getSanPham().getDuongDanAnh();
            m.put("anh", img);
            return m;
        }).toList());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/tra-cuu")
    public ResponseEntity<?> traCuu(@RequestParam String maHoaDon) {
        String cleanMa = maHoaDon.replace(": #", "").replace("#", "").trim();
        Optional<HoaDon> opt = hoaDonRepository.findByMaHoaDon(cleanMa);
        if (opt.isEmpty())
            return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy hóa đơn"));

        HoaDon hd = opt.get();
        Map<String, Object> hdMap = new HashMap<>();
        hdMap.put("id", hd.getId());
        hdMap.put("maHoaDon", hd.getMaHoaDon());
        hdMap.put("ngayDatHang", hd.getNgayDatHang() != null ? hd.getNgayDatHang() : hd.getNgayTao());
        hdMap.put("tenNguoiNhan", hd.getTenNguoiNhan());
        hdMap.put("soDienThoaiNguoiNhan", hd.getSoDienThoaiNguoiNhan());
        hdMap.put("emailNguoiNhan", hd.getEmailNguoiNhan());
        hdMap.put("diaChiGiao", String.format("%s, %s, %s, %s", hd.getChiTietNguoiNhan(), hd.getXaPhuong(),
                hd.getQuanHuyen(), hd.getTinhThanhPho()));
        hdMap.put("tongTien", hd.getTongTien());
        hdMap.put("phiShip", hd.getTienVanChuyen());
        hdMap.put("tienVanChuyen", hd.getTienVanChuyen());
        hdMap.put("tienPhieuGiamGia", hd.getTienPhieuGiamGia());
        hdMap.put("tongTienAfterGiam", hd.getTongTienAfterGiam());
        hdMap.put("trangThai", hd.getTrangThaiHoaDon());
        hdMap.put("phuongThucThanhToan", hd.getPhuongThucThanhToan());
        hdMap.put("ghiChu", hd.getMoTa());

        List<Map<String, Object>> items = chiTietHoaDonRepository.findByHoaDonId(hd.getId()).stream().map(ct -> {
            Map<String, Object> m = new HashMap<>();
            m.put("tenSanPham", ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
            m.put("mauSac", ct.getSanPhamChiTiet().getMauSac().getTenMauSac());
            m.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
            m.put("soLuong", ct.getSoLuong());
            m.put("gia", ct.getGia());
            String img = ct.getSanPhamChiTiet().getDuongDanAnh();
            if (img == null || img.isEmpty())
                img = ct.getSanPhamChiTiet().getSanPham().getDuongDanAnh();
            m.put("duongDanAnh", img);
            m.put("thanhTien", ct.getThanhTien());
            return m;
        }).toList();
        hdMap.put("chiTietSanPham", items);

        return ResponseEntity.ok(Map.of("success", true, "hoaDon", hdMap));
    }

    @GetMapping("/shipping-fee")
    public ResponseEntity<?> getShippingFee(@RequestParam String province, @RequestParam String district,
            @RequestParam(required = false) String ward) {
        try {
            Integer dId = ghnService.findDistrictId(province, district);
            String wCode = ghnService.findWardCode(dId, ward);
            return ResponseEntity.ok(Map.of("success", true, "fee", ghnService.calculateShippingFee(dId, wCode)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        try {
            com.fasterxml.jackson.databind.JsonNode data = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(ghnService.getProvinces()).path("data");
            List<Map<String, Object>> res = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode n : data)
                res.add(Map.of("code", n.path("ProvinceID").asInt(), "name", n.path("ProvinceName").asText()));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<?> getDistricts(@PathVariable Integer provinceId) {
        try {
            com.fasterxml.jackson.databind.JsonNode data = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(ghnService.getDistricts(provinceId)).path("data");
            List<Map<String, Object>> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode n : data)
                list.add(Map.of("code", n.path("DistrictID").asInt(), "name", n.path("DistrictName").asText()));
            return ResponseEntity.ok(Map.of("districts", list));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/wards/{districtId}")
    public ResponseEntity<?> getWards(@PathVariable Integer districtId) {
        try {
            com.fasterxml.jackson.databind.JsonNode data = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(ghnService.getWards(districtId)).path("data");
            List<Map<String, Object>> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode n : data)
                list.add(Map.of("code", n.path("WardCode").asText(), "name", n.path("WardName").asText()));
            return ResponseEntity.ok(Map.of("wards", list));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/address/add")
    public ResponseEntity<?> addAddress(@RequestBody Map<String, Object> body) {
        try {
            String tenNguoiNhan = (String) body.get("tenNguoiNhan");
            String soDienThoai = (String) body.get("soDienThoai");
            if (tenNguoiNhan == null || !tenNguoiNhan.matches("^[\\p{L}\\s]+$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên người nhận không được chứa số và ký tự đặc biệt!"));
            }
            if (soDienThoai == null || !soDienThoai.matches("^0\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số!"));
            }

            KhachHang kh = khachHangRepository.findById(Long.valueOf(body.get("khachHangId").toString())).orElse(null);
            if (kh == null)
                return ResponseEntity.badRequest().body("Not found");
            DiaChi dc = new DiaChi();
            dc.setKhachHang(kh);
            dc.setTenNguoiNhan(tenNguoiNhan);
            dc.setSoDienThoaiNguoiNhan(soDienThoai);
            dc.setTinhThanhPho((String) body.get("tinhThanhPho"));
            dc.setQuanHuyen((String) body.get("quanHuyen"));
            dc.setXaPhuong((String) body.get("xaPhuong"));
            dc.setChiTiet((String) body.get("chiTiet"));
            dc.setNgayTao(LocalDateTime.now());
            List<DiaChi> list = diaChiRepository.findByKhachHangId(kh.getId());
            if (list.isEmpty() || (body.get("diaChiMacDinh") != null && (boolean) body.get("diaChiMacDinh"))) {
                list.forEach(a -> a.setDiaChiMacDinh(false));
                diaChiRepository.saveAll(list);
                dc.setDiaChiMacDinh(true);
            }
            diaChiRepository.save(dc);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/address/update")
    public ResponseEntity<?> updateAddress(@RequestBody Map<String, Object> body) {
        try {
            String tenNguoiNhan = (String) body.get("tenNguoiNhan");
            String soDienThoai = (String) body.get("soDienThoai");
            if (tenNguoiNhan == null || !tenNguoiNhan.matches("^[\\p{L}\\s]+$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tên người nhận không được chứa số và ký tự đặc biệt!"));
            }
            if (soDienThoai == null || !soDienThoai.matches("^0\\d{9}$")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số!"));
            }

            DiaChi dc = diaChiRepository.findById(Long.valueOf(body.get("id").toString())).orElse(null);
            if (dc == null)
                return ResponseEntity.notFound().build();
            dc.setTenNguoiNhan(tenNguoiNhan);
            dc.setSoDienThoaiNguoiNhan(soDienThoai);
            dc.setTinhThanhPho((String) body.get("tinhThanhPho"));
            dc.setQuanHuyen((String) body.get("quanHuyen"));
            dc.setXaPhuong((String) body.get("xaPhuong"));
            dc.setChiTiet((String) body.get("chiTiet"));
            if (body.get("diaChiMacDinh") != null && (boolean) body.get("diaChiMacDinh")) {
                List<DiaChi> list = diaChiRepository.findByKhachHangId(dc.getKhachHang().getId());
                list.forEach(a -> a.setDiaChiMacDinh(false));
                diaChiRepository.saveAll(list);
                dc.setDiaChiMacDinh(true);
            }
            diaChiRepository.save(dc);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/address/delete/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        try {
            DiaChi dc = diaChiRepository.findById(id).orElse(null);
            if (dc == null)
                return ResponseEntity.notFound().build();
            if (dc.getDiaChiMacDinh())
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không thể xóa địa chỉ mặc định"));
            dc.setXoaMem(true);
            diaChiRepository.save(dc);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/address/set-default/{id}")
    public ResponseEntity<?> setDefault(@PathVariable Long id) {
        try {
            DiaChi dc = diaChiRepository.findById(id).orElse(null);
            if (dc == null)
                return ResponseEntity.notFound().build();
            List<DiaChi> list = diaChiRepository.findByKhachHangId(dc.getKhachHang().getId());
            list.forEach(a -> a.setDiaChiMacDinh(false));
            dc.setDiaChiMacDinh(true);
            diaChiRepository.saveAll(list);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}