package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.PhieuGiamGia;
import com.example.datn_shop_ecom.repository.PhieuGiamGiaRepository;
import com.example.datn_shop_ecom.service.PhieuGiamGiaService;
import com.example.datn_shop_ecom.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    @Autowired
    private PhieuGiamGiaRepository pggRepo;

    @Override
    public List<PhieuGiamGia> filterPhieuGiamGia(String search, LocalDate startDate, LocalDate endDate,
            Integer status) {
        if (search != null && search.trim().isEmpty())
            search = null;
        return pggRepo.findByFilters(search, startDate, endDate, status);
    }

    @Override
    public org.springframework.data.domain.Page<PhieuGiamGia> filterPhieuGiamGiaPage(String search, LocalDate startDate,
            LocalDate endDate, Integer status, org.springframework.data.domain.Pageable pageable) {
        if (search != null && search.trim().isEmpty())
            search = null;
        return pggRepo.findByFiltersPage(search, startDate, endDate, status, pageable);
    }

    @Override
    @Transactional
    public PhieuGiamGia savePGG(PhieuGiamGia pgg) {
        
        if (pgg.getTenPhieu() == null || pgg.getTenPhieu().trim().isEmpty()) {
            throw new RuntimeException("Tên phiếu giảm giá không được để trống");
        }
        if (pgg.getGiaTriGiam() == null || pgg.getGiaTriGiam().doubleValue() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }
        if ("%".equals(pgg.getHinhThucGiam())) {
            double val = pgg.getGiaTriGiam().doubleValue();
            if (val <= 1 || val > 100) {
                throw new RuntimeException("Giảm giá theo % phải lớn hơn 1% và không vượt quá 100%");
            }
        } else {
            // Giảm theo tiền mặt
            if (pgg.getGiaTriGiam().doubleValue() >= 100000000) {
                throw new RuntimeException("Giá trị giảm tiền mặt phải nhỏ hơn 100,000,000 VNĐ");
            }
        }
        if (pgg.getNgayBatDau() != null && pgg.getNgayKetThuc() != null) {
            if (pgg.getNgayKetThuc().isBefore(pgg.getNgayBatDau())) {
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }
        if (pgg.getSoLuong() != null && pgg.getSoLuong() < 1) {
            throw new RuntimeException("Số lượng phải ít nhất là 1");
        }
        if (pgg.getGiaTriToiThieu() != null && pgg.getGiaTriToiThieu().doubleValue() < 0) {
            throw new RuntimeException("Giá trị hóa đơn tối thiểu không được nhỏ hơn 0");
        }

        
        if (pgg.getId() != null) {
            PhieuGiamGia existing = pggRepo.findById(pgg.getId())
                    .orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));

            existing.setTenPhieu(pgg.getTenPhieu());
            existing.setNgayBatDau(pgg.getNgayBatDau());
            existing.setNgayKetThuc(pgg.getNgayKetThuc());
            existing.setHinhThucGiam(pgg.getHinhThucGiam());
            existing.setGiaTriGiam(pgg.getGiaTriGiam());
            existing.setGiaTriToiThieu(pgg.getGiaTriToiThieu());
            existing.setSoLuong(pgg.getSoLuong());
            existing.setTrangThai(pgg.getTrangThai());
            existing.setLoai(pgg.getLoai());
            existing.setMoTa(pgg.getMoTa());

            existing.setNgaySuaCuoi(LocalDateTime.now());
            existing.setNguoiSuaCuoi("Admin");

            return pggRepo.save(existing);
        } else {
            if (pgg.getMaPhieu() == null || pgg.getMaPhieu().isEmpty()) {
                pgg.setMaPhieu(generateMaPGG());
            }
            if (pgg.getNgayTao() == null) {
                pgg.setNgayTao(LocalDateTime.now());
            }
            if (pgg.getTrangThai() == null) {
                pgg.setTrangThai(1);
            }
            pgg.setXoaMem(false);
            pgg.setNguoiTao("Admin");

            return pggRepo.save(pgg);
        }
    }

    @Override
    public PhieuGiamGia findById(Long id) {
        return pggRepo.findById(id).orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));
    }

    @Override
    public void toggleStatus(Long id) {
        PhieuGiamGia pgg = findById(id);
        int newStatus = (pgg.getTrangThai() == 1) ? 0 : 1;
        pggRepo.updateStatus(id, newStatus, LocalDateTime.now(), "Admin");
    }

    @Override
    public void softDelete(Long id) {
        pggRepo.softDelete(id, LocalDateTime.now());
    }

    @Override
    public String generateMaPGG() {
        long count = pggRepo.count();
        return String.format("PGG%05d", count + 1);
    }

    @Override
    public ByteArrayInputStream exportToExcel(String search, LocalDate startDate, LocalDate endDate, Integer status) {
        List<PhieuGiamGia> list = filterPhieuGiamGia(search, startDate, endDate, status);
        String[] columns = { "STT", "Mã Phiếu", "Tên Phiếu", "Loại", "Số lượng", "Ngày Bắt Đầu", "Ngày Kết Thúc",
                "Giá Trị Giảm", "Hình Thức", "Trạng Thái" };

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return ExcelUtil.exportToExcel("Danh sách phiếu giảm giá", columns, list, (row, pgg) -> {
            row.createCell(0).setCellValue(list.indexOf(pgg) + 1);
            row.createCell(1).setCellValue(pgg.getMaPhieu() != null ? pgg.getMaPhieu() : "");
            row.createCell(2).setCellValue(pgg.getTenPhieu() != null ? pgg.getTenPhieu() : "");
            row.createCell(3).setCellValue(pgg.getLoai() != null && pgg.getLoai() == 1 ? "Cá nhân" : "Công khai");
            row.createCell(4).setCellValue(pgg.getSoLuong() != null ? pgg.getSoLuong() : 0);
            row.createCell(5).setCellValue(pgg.getNgayBatDau() != null ? pgg.getNgayBatDau().format(dtf) : "-");
            row.createCell(6).setCellValue(pgg.getNgayKetThuc() != null ? pgg.getNgayKetThuc().format(dtf) : "-");
            row.createCell(7).setCellValue(pgg.getGiaTriGiam() != null ? pgg.getGiaTriGiam().toString() : "0");
            row.createCell(8).setCellValue(pgg.getHinhThucGiam() != null ? pgg.getHinhThucGiam() : "");
            row.createCell(9).setCellValue(
                    pgg.getTrangThai() != null && pgg.getTrangThai() == 1 ? "Hoạt động" : "Ngừng hoạt động");
        });
    }

    @Override
    public List<PhieuGiamGia> findAllByXoaMemFalse() {
        return pggRepo.findAllByXoaMemFalse();
    }
}

