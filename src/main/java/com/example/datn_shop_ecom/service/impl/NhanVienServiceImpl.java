package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.repository.NhanVienRepository;
import com.example.datn_shop_ecom.service.EmailService;
import com.example.datn_shop_ecom.service.NhanVienService;
import com.example.datn_shop_ecom.util.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<NhanVien> filterNhanVien(String search, Long idVaiTro, Boolean xoaMem) {
        if (search != null && search.trim().isEmpty())
            search = null;
        return nhanVienRepository.findByFilters(search, idVaiTro, xoaMem);
    }

    @Override
    public org.springframework.data.domain.Page<NhanVien> filterNhanVienPage(String search, Long idVaiTro,
            Boolean xoaMem, org.springframework.data.domain.Pageable pageable) {
        if (search != null && search.trim().isEmpty())
            search = null;
        return nhanVienRepository.findByFiltersPage(search, idVaiTro, xoaMem, pageable);
    }

    @Override
    public List<NhanVien> getAllNhanViens() {
        return nhanVienRepository.findAllByXoaMemFalse();
    }

    @Override
    @Transactional
    public NhanVien saveNhanVien(NhanVien nhanVien, org.springframework.web.multipart.MultipartFile anhFile) {
        // Handle file upload
        if (anhFile != null && !anhFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/nhan-vien/";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                String fileName = java.util.UUID.randomUUID().toString() + "_" + anhFile.getOriginalFilename();
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(anhFile.getInputStream(), filePath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                nhanVien.setAnh(fileName);
            } catch (java.io.IOException e) {
                System.err.println("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        if (nhanVien.getId() != null) {
            NhanVien existing = nhanVienRepository.findById(nhanVien.getId())
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

            existing.setTenDayDu(nhanVien.getTenDayDu());
            existing.setEmail(nhanVien.getEmail());
            existing.setSoDienThoai(nhanVien.getSoDienThoai());
            existing.setGioiTinh(nhanVien.getGioiTinh());
            existing.setNgaySinh(nhanVien.getNgaySinh());
            existing.setDiaChi(nhanVien.getDiaChi());
            existing.setXaPhuong(nhanVien.getXaPhuong());
            existing.setQuanHuyen(nhanVien.getQuanHuyen());
            existing.setTinhThanhPho(nhanVien.getTinhThanhPho());
            existing.setVaiTro(nhanVien.getVaiTro());

            if (nhanVien.getAnh() != null) {
                existing.setAnh(nhanVien.getAnh());
            }

            existing.setNgaySuaCuoi(LocalDateTime.now());
            existing.setNguoiSuaCuoi("Admin");

            return nhanVienRepository.save(existing);
        } else {
            if (nhanVien.getMaNhanVien() == null || nhanVien.getMaNhanVien().isEmpty()) {
                nhanVien.setMaNhanVien(generateMaNhanVien());
            }
            if (nhanVien.getNgayTao() == null) {
                nhanVien.setNgayTao(LocalDateTime.now());
            }
            nhanVien.setXoaMem(false);

            String password = generateRandomPassword(8);
            nhanVien.setMatKhau(password);
            nhanVien.setNguoiTao("Admin");

            NhanVien saved = nhanVienRepository.save(nhanVien);

            try {
                String subject = "Tài khoản nhân viên PeakSneaker ";
                String body = String.format(
                        "Chào mừng %s!\n\n" +
                                "Tài khoản nhân viên của bạn đã được tạo thành công.\n" +
                                "Thông tin đăng nhập:\n" +
                                "- Mã NV: %s\n" +
                                "- Email: %s\n" +
                                "- Mật khẩu: %s\n\n" +
                                "Vui lòng đăng nhập và đổi mật khẩu để bảo mật.\n" +
                                "Trân trọng!",
                        saved.getTenDayDu(), saved.getMaNhanVien(), saved.getEmail(), password);
                emailService.sendEmail(saved.getEmail(), subject, body);
            } catch (Exception e) {
                System.err.println("Lỗi gửi mail: " + e.getMessage());
            }

            return saved;
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    public NhanVien findById(Long id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
    }

    @Override
    public void toggleStatus(Long id) {
        NhanVien nhanVien = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        nhanVienRepository.updateStatus(id, !nhanVien.getXoaMem(), "Admin");
    }

    @Override
    public String generateMaNhanVien() {
        long count = nhanVienRepository.count();
        return String.format("NV%05d", count + 1);
    }

    @Override
    public ByteArrayInputStream exportToExcel(String search, Long idVaiTro, Boolean xoaMem) {
        List<NhanVien> list = filterNhanVien(search, idVaiTro, xoaMem);
        String[] columns = { "STT", "Mã NV", "Họ Tên", "Chức Vụ", "SĐT", "Email", "Địa Chỉ", "Trạng Thái" };

        return ExcelUtil.exportToExcel("Danh sách nhân viên", columns, list, (row, nv) -> {
            row.createCell(0).setCellValue(list.indexOf(nv) + 1);
            row.createCell(1).setCellValue(nv.getMaNhanVien());
            row.createCell(2).setCellValue(nv.getTenDayDu());
            row.createCell(3).setCellValue(nv.getVaiTro() != null ? nv.getVaiTro().getTen() : "-");
            row.createCell(4).setCellValue(nv.getSoDienThoai());
            row.createCell(5).setCellValue(nv.getEmail());

            String diaChiStr = (nv.getDiaChi() != null ? nv.getDiaChi() + ", " : "")
                    + (nv.getXaPhuong() != null ? nv.getXaPhuong() + ", " : "")
                    + (nv.getQuanHuyen() != null ? nv.getQuanHuyen() + ", " : "")
                    + (nv.getTinhThanhPho() != null ? nv.getTinhThanhPho() : "");
            row.createCell(6).setCellValue(diaChiStr.trim().isEmpty() ? "-" : diaChiStr);
            row.createCell(7).setCellValue(nv.getXoaMem() != null && nv.getXoaMem() ? "Ngừng hoạt động" : "Hoạt động");
        });
    }
}
