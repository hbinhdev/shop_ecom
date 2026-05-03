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

        LocalDate today = LocalDate.now();
        if (pgg.getNgayBatDau() != null && pgg.getNgayKetThuc() != null) {
            if (pgg.getNgayKetThuc().isBefore(pgg.getNgayBatDau())) {
                throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
            }
        }

        // Fix 1: Thêm ngày thì phải từ ngày hôm nay hoặc là tương lai (khi tạo mới)
        if (pgg.getId() == null) {
            if (pgg.getNgayBatDau() != null && pgg.getNgayBatDau().isBefore(today)) {
                throw new RuntimeException("Ngày bắt đầu không được ở quá khứ");
            }
        }

        if (pgg.getSoLuong() != null && pgg.getSoLuong() < 1) {
            throw new RuntimeException("Số lượng phải ít nhất là 1");
        }
        if (pgg.getGiaTriToiThieu() != null && pgg.getGiaTriToiThieu().doubleValue() < 0) {
            throw new RuntimeException("Giá trị hóa đơn tối thiểu không được nhỏ hơn 0");
        }

        // Tự động tính toán trạng thái dựa trên ngày
        pgg.setTrangThai(calculateStatus(pgg));

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
            pgg.setXoaMem(false);
            pgg.setNguoiTao("Admin");

            return pggRepo.save(pgg);
        }
    }

    private Integer calculateStatus(PhieuGiamGia pgg) {
        LocalDate today = LocalDate.now();
        if (pgg.getNgayBatDau() != null && today.isBefore(pgg.getNgayBatDau())) {
            return 2; // Chưa bắt đầu
        }
        if (pgg.getNgayKetThuc() != null && today.isAfter(pgg.getNgayKetThuc())) {
            return 0; // Đã kết thúc
        }
        // Nếu đã qua ngày kết thúc hoặc số lượng hết thì có thể coi là kết thúc
        // Tuy nhiên ở đây chỉ tính theo ngày theo yêu cầu
        return 1; // Đang hoạt động
    }

    @Override
    public PhieuGiamGia findById(Long id) {
        return pggRepo.findById(id).orElseThrow(() -> new RuntimeException("Phiếu giảm giá không tồn tại"));
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        PhieuGiamGia pgg = findById(id);
        // Nếu đang hoạt động (1) hoặc chưa bắt đầu (2) -> Ngừng hoạt động (3)
        // Nếu đang Ngừng hoạt động (3) -> Kích hoạt lại (tính toán theo ngày)
        
        int newStatus;
        if (pgg.getTrangThai() == 1 || pgg.getTrangThai() == 2) {
            newStatus = 3; // Chuyển sang Ngừng hoạt động
        } else if (pgg.getTrangThai() == 3) {
            newStatus = calculateStatus(pgg);
            if (newStatus == 0) {
                throw new RuntimeException("Phiếu này đã hết hạn, không thể kích hoạt lại");
            }
        } else {
            throw new RuntimeException("Phiếu này đã kết thúc, không thể thay đổi trạng thái");
        }
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
            
            String statusText = "Ngừng hoạt động";
            if (pgg.getTrangThai() == 1) statusText = "Đang hoạt động";
            else if (pgg.getTrangThai() == 2) statusText = "Chưa bắt đầu";
            else if (pgg.getTrangThai() == 0) statusText = "Đã kết thúc";
            
            row.createCell(9).setCellValue(statusText);
        });
    }

    @Override
    @Transactional
    public void updateAllStatuses() {
        List<PhieuGiamGia> list = pggRepo.findAll();
        for (PhieuGiamGia pgg : list) {
            if (!pgg.getXoaMem()) {
                Integer currentStatus = pgg.getTrangThai();
                Integer calculatedStatus = calculateStatus(pgg);

                boolean changed = false;
                
                // Nếu đang Ngừng hoạt động (3), chỉ tự động chuyển sang Đã kết thúc (0) nếu hết hạn
                if (currentStatus == 3) {
                    if (calculatedStatus == 0) {
                        pgg.setTrangThai(0);
                        changed = true;
                    }
                } else {
                    // Các trạng thái khác tự động cập nhật theo ngày
                    if (currentStatus != calculatedStatus && currentStatus != 3) {
                        pgg.setTrangThai(calculatedStatus);
                        changed = true;
                    }
                }

                if (changed) {
                    pgg.setNgaySuaCuoi(LocalDateTime.now());
                    pgg.setNguoiSuaCuoi("System");
                    pggRepo.save(pgg);
                }
            }
        }
    }

    @Override
    public List<PhieuGiamGia> findAllByXoaMemFalse() {
        return pggRepo.findAllByXoaMemFalse();
    }
}

