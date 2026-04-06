package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.SanPham;
import com.example.datn_shop_ecom.repository.SanPhamChiTietRepository;
import com.example.datn_shop_ecom.repository.SanPhamRepository;
import com.example.datn_shop_ecom.service.SanPhamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepo;

    @Autowired
    private SanPhamChiTietRepository spctRepo;

    @Override
    public Page<SanPham> filterSanPhamPage(String search, Pageable pageable) {
        if (search != null && search.trim().isEmpty()) search = null;
        return sanPhamRepo.findByFilters(search, pageable);
    }

    @Override
    public Page<SanPham> filterSanPham(String search, Pageable pageable) {
        if (search != null && search.trim().isEmpty()) search = null;
        return sanPhamRepo.findByFilters(search, pageable);
    }

    @Override
    @Transactional
    public SanPham saveSanPham(SanPham sanPham) {
        if (sanPham.getId() != null) {
            SanPham existing = sanPhamRepo.findById(sanPham.getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            existing.setTenSanPham(sanPham.getTenSanPham());
            existing.setMoTa(sanPham.getMoTa());
            existing.setNgaySuaCuoi(LocalDateTime.now());
            return sanPhamRepo.save(existing);
        } else {
            if (sanPham.getMaSanPham() == null || sanPham.getMaSanPham().isEmpty()) {
                long count = sanPhamRepo.count();
                sanPham.setMaSanPham(String.format("SP%05d", count + 1));
            }
            sanPham.setNgayTao(LocalDateTime.now());
            sanPham.setXoaMem(false); // false = Đang kinh doanh
            sanPham.setNguoiTao("Admin");
            return sanPhamRepo.save(sanPham);
        }
    }

    @Override
    public SanPham findById(Long id) {
        return sanPhamRepo.findById(id).orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        SanPham sp = findById(id);
        boolean currentStatus = sp.getXoaMem() != null ? sp.getXoaMem() : false;
        boolean newStatus = !currentStatus;
        
        // 1. Cập nhật trạng thái sản phẩm
        sanPhamRepo.updateXoaMem(id, newStatus);
        
        // 2. Logic đồng bộ trạng thái SPCT (Nếu xoaMem = true => Ngừng hoạt động ('0'), xoaMem = false => Hoạt động ('1'))
        String newSpctStatus = newStatus ? "0" : "1";
        spctRepo.updateTrangThaiBySanPhamId(id, newSpctStatus);
    }

    @Override
    public java.io.ByteArrayInputStream exportToExcel(String search) {
        if (search != null && search.trim().isEmpty()) search = null;
        // Sử dụng repo để lấy tất cả (không phân trang cho export)
        java.util.List<SanPham> dataList = sanPhamRepo.findByFilters(search, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        String[] columns = {"STT", "Mã sản phẩm", "Tên sản phẩm", "Ngày tạo", "Trạng thái"};
        
        return com.example.datn_shop_ecom.util.ExcelUtil.exportToExcel("Danh sách sản phẩm", columns, dataList, (row, sp) -> {
            row.createCell(0).setCellValue(row.getRowNum());
            row.createCell(1).setCellValue(sp.getMaSanPham());
            row.createCell(2).setCellValue(sp.getTenSanPham());
            row.createCell(3).setCellValue(sp.getNgayTao() != null ? sp.getNgayTao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            row.createCell(4).setCellValue(sp.getXoaMem() != null && sp.getXoaMem() ? "Ngừng kinh doanh" : "Đang kinh doanh");
        });
    }
}
