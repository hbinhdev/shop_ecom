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
    private AuthenticationManager authenticationManager;

    @GetMapping("/auth/get-token")
    public ResponseEntity<?> getToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String token = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(Map.of("token", token));
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email") != null ? body.get("email").trim() : "";
        String password = body.get("password");
        Map<String, Object> resp = new HashMap<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

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
            } else {
                // Trường hợp Admin/Nhân viên đăng nhập vào trang Client
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
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "Lỗi đăng nhập: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
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
            
            if (body.get("ngaySinh") != null && !body.get("ngaySinh").toString().isEmpty()) {
                kh.setNgaySinh(java.time.LocalDate.parse(body.get("ngaySinh").toString()));
            }

            // Xử lý danh sách địa chỉ nếu có
            if (body.get("addresses") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> addrList = (List<Map<String, Object>>) body.get("addresses");
                for (Map<String, Object> addrMap : addrList) {
                    DiaChi dc = new DiaChi();
                    dc.setTenNguoiNhan((String) addrMap.get("tenNguoiNhan"));
                    dc.setTinhThanhPho((String) addrMap.get("tinhThanhPho"));
                    dc.setQuanHuyen((String) addrMap.get("quanHuyen"));
                    dc.setXaPhuong((String) addrMap.get("xaPhuong"));
                    dc.setChiTiet((String) addrMap.get("chiTiet"));
                    dc.setDiaChiMacDinh(true); // Mặc định là địa chỉ đầu tiên
                    kh.getDanhSachDiaChi().add(dc);
                }
            }

            khachHangService.registerKhachHang(kh);
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("message", "Đăng ký thất bại: " + e.getMessage());
            return ResponseEntity.status(400).body(resp);
        }
    }

    @PostMapping("/re-auth")
    public ResponseEntity<?> reAuth(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        Map<String, Object> resp = new HashMap<>();
        
        Optional<KhachHang> opt = khachHangRepository.findByEmail(email);
        if (opt.isPresent()) {
            KhachHang kh = opt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                kh.getEmail(), null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.status(401).body(Map.of("success", false));
    }

    @GetMapping("/vouchers/best")
    public ResponseEntity<?> getBestVoucher(@RequestParam Double cartTotal, @RequestParam(required = false) Long khId) {
        try {
            List<PhieuGiamGia> list = phieuGiamGiaRepository.findAllByXoaMemFalse();
            LocalDate now = LocalDate.now();
            
            // Nếu có khId, lấy danh sách ID các voucher đã dùng
            List<Long> usedVoucherIds = new ArrayList<>();
            if (khId != null) {
                usedVoucherIds = hoaDonRepository.findUsedVoucherIdsByKhachHangId(khId);
            }
            
            final List<Long> finalUsedIds = usedVoucherIds;
            Optional<PhieuGiamGia> best = list.stream()
                .filter(v -> !finalUsedIds.contains(v.getId())) // Loại bỏ voucher đã dùng
                .filter(v -> v.getNgayBatDau() != null && (v.getNgayBatDau().isBefore(now) || v.getNgayBatDau().isEqual(now)))
                .filter(v -> v.getNgayKetThuc() != null && (v.getNgayKetThuc().isAfter(now) || v.getNgayKetThuc().isEqual(now)))
                .filter(v -> v.getTrangThai() != null && v.getTrangThai() == 1)
                .filter(v -> v.getSoLuong() == null || v.getSoLuong() > 0)
                .filter(v -> v.getGiaTriToiThieu() != null && v.getGiaTriToiThieu().doubleValue() <= cartTotal)
                .max(Comparator.comparing(v -> {
                    double discountValue = 0;
                    if ("VNĐ".equals(v.getHinhThucGiam())) {
                        discountValue = v.getGiaTriGiam().doubleValue();
                    } else {
                        discountValue = cartTotal * (v.getGiaTriGiam().doubleValue() / 100.0);
                    }
                    return discountValue;
                }));

            if (best.isPresent()) {
                PhieuGiamGia v = best.get();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("id", v.getId());
                response.put("ma", v.getMaPhieu());
                response.put("hinhThuc", v.getHinhThucGiam());
                response.put("giaTri", v.getGiaTriGiam());
                response.put("discountLabel", "VNĐ".equals(v.getHinhThucGiam()) ? 
                    String.format("%,.0f đ", v.getGiaTriGiam()) : v.getGiaTriGiam() + "%");
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(Map.of("success", false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/vouchers/used/{khId}")
    public ResponseEntity<?> getUsedVouchers(@PathVariable Long khId) {
        try {
            // Sử dụng hàm có sẵn trong repository để lấy thẳng danh sách ID voucher đã dùng
            List<Long> usedIds = hoaDonRepository.findUsedVoucherIdsByKhachHangId(khId);
            return ResponseEntity.ok(usedIds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @PostMapping("/validate-cart")
    public ResponseEntity<?> validateCart(@RequestBody List<Map<String, Object>> items) {
        List<Map<String, Object>> errors = new ArrayList<>();
        boolean success = true;

        for (Map<String, Object> item : items) {
            Long spctId = Long.valueOf(item.get("spctId").toString());
            int soLuongYeuCau = Integer.parseInt(item.get("soLuong").toString());
            
            SanPhamChiTiet spct = spctRepository.findById(spctId).orElse(null);
            if (spct == null || spct.getSoTonKho() < soLuongYeuCau) {
                success = false;
                Map<String, Object> err = new HashMap<>();
                err.put("spctId", spctId);
                err.put("tenSanPham", spct != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm không tồn tại");
                err.put("soLuongYeuCau", soLuongYeuCau);
                err.put("soTonKho", spct != null ? spct.getSoTonKho() : 0);
                errors.add(err);
            }
        }

        return ResponseEntity.ok(Map.of("success", success, "errors", errors));
    }

    @PostMapping("/place-order")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
        try {
            Long khId = body.get("khId") != null ? Long.valueOf(body.get("khId").toString()) : null;
            String ten = (String) body.get("tenNguoiNhan");
            String sdt = (String) body.get("soDienThoaiNguoiNhan");
            String ttp = (String) body.get("tinhThanhPho");
            String qh = (String) body.get("quanHuyen");
            String xp = (String) body.get("xaPhuong");
            String ct = (String) body.get("chiTiet");
            double phiShip = Double.parseDouble(body.get("phiShip").toString());
            Long voucherId = body.get("voucherId") != null ? Long.valueOf(body.get("voucherId").toString()) : null;
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("cartItems");

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
            hd.setNgayDatHang(LocalDateTime.now());
            hd.setNgayTao(LocalDateTime.now());
            hd.setTrangThaiHoaDon("CHO_XAC_NHAN");
            hd.setLoaiHoaDon("Giao hàng");
            hd.setPhuongThucThanhToan("Thanh toán bằng tiền mặt");
            hd.setEmailNguoiNhan((String) body.get("email"));
            
            java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
            if (voucherId != null) {
                PhieuGiamGia v = phieuGiamGiaRepository.findById(voucherId).orElse(null);
                if (v != null) {
                    hd.setPhieuGiamGia(v);
                    if ("VNĐ".equals(v.getHinhThucGiam())) {
                        discountAmount = v.getGiaTriGiam();
                    } else {
                        discountAmount = subTotal.multiply(v.getGiaTriGiam()).divide(java.math.BigDecimal.valueOf(100));
                    }
                    if (discountAmount.compareTo(subTotal) > 0) discountAmount = subTotal;
                    if (v.getSoLuong() != null) {
                        v.setSoLuong(v.getSoLuong() - 1);
                        phieuGiamGiaRepository.save(v);
                    }
                }
            }
            
            hd.setTienPhieuGiamGia(discountAmount);
            hd.setTongTienAfterGiam(subTotal.add(hd.getTienVanChuyen()).subtract(discountAmount));

            HoaDon savedHd = hoaDonRepository.save(hd);

            if (khId != null) {
                Optional<DiaChi> exstDiaChi = diaChiRepository.findByKhachHangIdAndTenNguoiNhanAndSoDienThoaiNguoiNhanAndTinhThanhPhoAndQuanHuyenAndXaPhuongAndChiTiet(
                    khId, ten, sdt, ttp, qh, xp, ct
                );
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
                    cthd.setThanhTien(spct.getGiaBan().multiply(java.math.BigDecimal.valueOf(qty)));
                    chiTietHoaDonRepository.save(cthd);
                    
                    spct.setSoTonKho(spct.getSoTonKho() - qty);
                    spctRepository.save(spct);
                }
            }

            if (khId != null) {
                gioHangService.clearCart(khId);
            }

            return ResponseEntity.ok(Map.of("success", true, "orderId", savedHd.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/profile/{khId}")
    public ResponseEntity<?> getProfile(@PathVariable Long khId) {
        try {
            KhachHang kh = khachHangRepository.findById(khId).orElse(null);
            if (kh == null) return ResponseEntity.notFound().build();
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", kh.getId());
            map.put("tenDayDu", kh.getTenDayDu());
            map.put("email", kh.getEmail());
            map.put("soDienThoai", kh.getSoDienThoai());
            
            // Lấy danh sách địa chỉ của khách hàng
            List<DiaChi> diaChis = diaChiRepository.findByKhachHangId(khId);
            System.out.println("DEBUG: Profile user " + khId + " co " + diaChis.size() + " dia chi.");
            map.put("danhSachDiaChi", diaChis);
            
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        try {
            String rawJson = ghnService.getProvinces();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode data = mapper.readTree(rawJson).path("data");
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : data) {
                Map<String, Object> m = new HashMap<>();
                m.put("code", node.path("ProvinceID").asInt());
                m.put("name", node.path("ProvinceName").asText());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/districts/{provinceId}")
    public ResponseEntity<?> getDistricts(@PathVariable Integer provinceId) {
        try {
            String rawJson = ghnService.getDistricts(provinceId);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode data = mapper.readTree(rawJson).path("data");
            
            List<Map<String, Object>> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : data) {
                Map<String, Object> m = new HashMap<>();
                m.put("code", node.path("DistrictID").asInt());
                m.put("name", node.path("DistrictName").asText());
                list.add(m);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("districts", list);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/wards/{districtId}")
    public ResponseEntity<?> getWards(@PathVariable Integer districtId) {
        try {
            String rawJson = ghnService.getWards(districtId);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode data = mapper.readTree(rawJson).path("data");
            
            List<Map<String, Object>> list = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : data) {
                Map<String, Object> m = new HashMap<>();
                m.put("code", node.path("WardCode").asText());
                m.put("name", node.path("WardName").asText());
                list.add(m);
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("wards", list);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping("/shipping-fee")
    public ResponseEntity<?> getShippingFee(@RequestParam String province, @RequestParam String district, @RequestParam(required = false) String ward) {
        try {
            // GHN cần DistrictID và WardCode để tính phí
            Integer dId = ghnService.findDistrictId(province, district);
            String wCode = ghnService.findWardCode(dId, ward);
            Integer fee = ghnService.calculateShippingFee(dId, wCode);
            
            return ResponseEntity.ok(Map.of("success", true, "fee", fee));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}