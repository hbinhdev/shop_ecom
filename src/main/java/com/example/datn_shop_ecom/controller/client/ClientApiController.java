package com.example.datn_shop_ecom.controller.client;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import com.example.datn_shop_ecom.service.KhachHangService;
import com.example.datn_shop_ecom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

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
                kh.setNgaySinh(LocalDate.parse(ngaySinhStr));
            }

            // Chuyển đổi danh sách địa chỉ
            List<Map<String, String>> addrList = (List<Map<String, String>>) body.get("addresses");
            List<DiaChi> danhSachDiaChi = new ArrayList<>();
            if (addrList != null) {
                for (Map<String, String> a : addrList) {
                    DiaChi dc = new DiaChi();
                    dc.setTenNguoiNhan(a.get("tenNguoiNhan"));
                    dc.setSoDienThoaiNguoiNhan(kh.getSoDienThoai()); // Mặc định lấy SĐT khách
                    dc.setTinhThanhPho(a.get("tinhThanhPho"));
                    dc.setQuanHuyen(a.get("quanHuyen"));
                    dc.setXaPhuong(a.get("xaPhuong"));
                    dc.setChiTiet(a.get("chiTiet"));
                    dc.setDiaChiMacDinh(danhSachDiaChi.isEmpty()); // Cái đầu tiên làm mặc định
                    danhSachDiaChi.add(dc);
                }
            }
            kh.setDanhSachDiaChi(danhSachDiaChi);

            // GỌI SERVICE ĐỂ TỰ SINH MÃ VÀ GỬI MAIL MẬT KHẨU
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
            Long id = Long.parseLong(body.get("id").toString());
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
            user.put("token", body.get("token")); // retain token

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

    private ResponseEntity<String> callExternal(String urlStr) {
        try {
            URL url = new URL(urlStr);
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
                spMap.put("duongDanAnh", ct.getSanPhamChiTiet().getDuongDanAnh());
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
