package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.service.KhachHangService;
import com.example.datn_shop_ecom.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.datn_shop_ecom.entity.DiaChi;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private com.example.datn_shop_ecom.service.EmailService emailService;

    @Override
    public java.util.List<KhachHang> filterKhachHang(String search, String gioiTinh, Boolean xoaMem) {
        if (search != null && search.trim().isEmpty())
            search = null;
        if (gioiTinh != null && gioiTinh.trim().isEmpty())
            gioiTinh = null;
        return khachHangRepository.findByFilters(search, gioiTinh, xoaMem);
    }

    @Override
    public org.springframework.data.domain.Page<KhachHang> filterKhachHangPage(String search, String gioiTinh,
            Boolean xoaMem, org.springframework.data.domain.Pageable pageable) {
        if (search != null && search.trim().isEmpty())
            search = null;
        if (gioiTinh != null && gioiTinh.trim().isEmpty())
            gioiTinh = null;
        return khachHangRepository.findByFiltersPage(search, gioiTinh, xoaMem, pageable);
    }

    @Override
    public java.util.List<KhachHang> getAllKhachHangs() {
        return khachHangRepository.findAllByXoaMemFalse();
    }

    @Override
    @Transactional
    public KhachHang saveKhachHang(KhachHang khachHang) {
        
        if (khachHang.getTenDayDu() == null || khachHang.getTenDayDu().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống");
        }
        if (khachHang.getEmail() == null || !khachHang.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email không đúng định dạng");
        }
        if (khachHang.getSoDienThoai() == null || !khachHang.getSoDienThoai().matches("0\\d{9}")) {
            throw new RuntimeException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0");
        }
        if (khachHang.getNgaySinh() != null && khachHang.getNgaySinh().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("Ngày sinh không được ở tương lai");
        }

        
        if (khachHangRepository.existsByEmail(khachHang.getEmail())) {
            Optional<com.example.datn_shop_ecom.entity.KhachHang> existing = khachHangRepository.findByEmail(khachHang.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(khachHang.getId())) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống!");
            }
        }

        
        if (khachHangRepository.existsBySoDienThoai(khachHang.getSoDienThoai())) {
            Optional<com.example.datn_shop_ecom.entity.KhachHang> existing = khachHangRepository.findBySoDienThoai(khachHang.getSoDienThoai());
            if (existing.isPresent() && !existing.get().getId().equals(khachHang.getId())) {
                throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống!");
            }
        }

        if (khachHang.getId() != null) {
            KhachHang existing = khachHangRepository.findById(khachHang.getId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

            existing.setTenDayDu(khachHang.getTenDayDu());
            existing.setEmail(khachHang.getEmail());
            existing.setSoDienThoai(khachHang.getSoDienThoai());

            if (khachHang.getGioiTinh() != null && !khachHang.getGioiTinh().trim().isEmpty()) {
                existing.setGioiTinh(khachHang.getGioiTinh());
            }
            if (khachHang.getNgaySinh() != null) {
                existing.setNgaySinh(khachHang.getNgaySinh());
            }

            existing.setNgaySuaCuoi(LocalDateTime.now());

            existing.getDanhSachDiaChi().clear();
            if (khachHang.getDanhSachDiaChi() != null) {
                for (com.example.datn_shop_ecom.entity.DiaChi dc : khachHang.getDanhSachDiaChi()) {
                    dc.setKhachHang(existing);
                    dc.setNgayTao(LocalDateTime.now());
                    if (dc.getDiaChiMacDinh() == null)
                        dc.setDiaChiMacDinh(false);
                    existing.getDanhSachDiaChi().add(dc);
                }
            }
            return khachHangRepository.save(existing);
        } else {
            if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
                khachHang.setMaKhachHang(generateMaKhachHang());
            }
            if (khachHang.getNgayTao() == null) {
                khachHang.setNgayTao(LocalDateTime.now());
            }
            khachHang.setXoaMem(false);
            
            String password = generateRandomPassword(8);
            khachHang.setMatKhau(password);

            if (khachHang.getDanhSachDiaChi() != null) {
                khachHang.getDanhSachDiaChi().forEach(dc -> {
                    dc.setKhachHang(khachHang);
                    dc.setNgayTao(LocalDateTime.now());
                    if (dc.getDiaChiMacDinh() == null)
                        dc.setDiaChiMacDinh(false);
                });
            }

            KhachHang saved = khachHangRepository.save(khachHang);

            try {
                String subject = "Tài khoản đăng nhập PeakSneaker ";
                String body = String.format(
                        "Chào mừng %s!\n\n" +
                                "Tài khoản của bạn đã được tạo thành công.\n" +
                                "Thông tin đăng nhập:\n" +
                                "- Email: %s\n" +
                                "- Mật khẩu: %s\n\n" +
                                "Vui lòng đăng nhập và đổi mật khẩu để bảo mật tài khoản.\n" +
                                "Trân trọng!",
                        saved.getTenDayDu(), saved.getEmail(), password);
                emailService.sendEmail(saved.getEmail(), subject, body);
            } catch (Exception e) {
                
            }

            return saved;
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456389";
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    public String generateMaKhachHang() {
        long count = khachHangRepository.count();
        return String.format("KH%05d", count + 1);
    }

    @Override
    public KhachHang findById(Long id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
    }

    @Override
    public void toggleStatus(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        khachHangRepository.updateStatus(id, !khachHang.getXoaMem());
    }

    @Override
    public ByteArrayInputStream exportToExcel(String search, String gioiTinh, Boolean xoaMem) {
        List<KhachHang> list = filterKhachHang(search, gioiTinh, xoaMem);
        String[] columns = { "STT", "Mã KH", "Họ Tên", "Giới Tính", "SĐT", "Email", "Địa Chỉ", "Trạng Thái" };

        return ExcelUtil.exportToExcel("Danh sách khách hàng", columns, list, (row, kh) -> {
            row.createCell(0).setCellValue(list.indexOf(kh) + 1);
            row.createCell(1).setCellValue(kh.getMaKhachHang());
            row.createCell(2).setCellValue(kh.getTenDayDu());
            row.createCell(3).setCellValue(kh.getGioiTinh() != null ? kh.getGioiTinh() : "-");
            row.createCell(4).setCellValue(kh.getSoDienThoai());
            row.createCell(5).setCellValue(kh.getEmail());

            String diaChiStr = "-";
            if (kh.getDanhSachDiaChi() != null && !kh.getDanhSachDiaChi().isEmpty()) {
                DiaChi macDinh = kh.getDanhSachDiaChi().stream()
                        .filter(d -> d.getDiaChiMacDinh() != null && d.getDiaChiMacDinh())
                        .findFirst().orElse(kh.getDanhSachDiaChi().get(0));
                diaChiStr = (macDinh.getChiTiet() != null ? macDinh.getChiTiet() + ", " : "")
                        + macDinh.getXaPhuong() + ", " + macDinh.getQuanHuyen() + ", " + macDinh.getTinhThanhPho();
            }
            row.createCell(6).setCellValue(diaChiStr);
            row.createCell(7).setCellValue(kh.getXoaMem() != null && kh.getXoaMem() ? "Ngừng hoạt động" : "Hoạt động");
        });
    }
}

