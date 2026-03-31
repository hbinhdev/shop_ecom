package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.util.List;
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
    public java.util.List<KhachHang> getAllKhachHangs() {
        return khachHangRepository.findAllByXoaMemFalse();
    }

    @Override
    @Transactional
    public KhachHang saveKhachHang(KhachHang khachHang) {
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
            // === Tự động sinh mật khẩu (VD: 8 ký tự) ===
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
                String subject = "Tài khoản đăng nhập SevenStrike";
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
                System.err.println("Lỗi gửi mail: " + e.getMessage());
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

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách khách hàng");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            String[] columns = { "STT", "Mã KH", "Họ Tên", "Giới Tính", "SĐT", "Email", "Địa Chỉ", "Trạng Thái" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (int i = 0; i < list.size(); i++) {
                KhachHang kh = list.get(i);
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(i + 1);
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
                row.createCell(7)
                        .setCellValue(kh.getXoaMem() != null && kh.getXoaMem() ? "Ngừng hoạt động" : "Hoạt động");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }
}
