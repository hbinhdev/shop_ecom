package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import com.example.datn_shop_ecom.repository.SanPhamChiTietRepository;
import com.example.datn_shop_ecom.service.SanPhamChiTietService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {

    @Autowired
    private SanPhamChiTietRepository repository;

    @Override
    public Page<SanPhamChiTiet> filterVariantPage(
            String search, Long idMauSac, Long idKichThuoc,
            BigDecimal minPrice, BigDecimal maxPrice,
            String trangThai, Long idSanPham,
            Long idThuongHieu, Long idDanhMuc,
            Pageable pageable
    ) {
        return repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("maSanPhamChiTiet")), keyword),
                    cb.like(cb.lower(root.get("sanPham").get("tenSanPham")), keyword),
                    cb.like(cb.lower(root.get("sanPham").get("maSanPham")), keyword)
                ));
            }

            if (idSanPham != null && idSanPham > 0) {
                predicates.add(cb.equal(root.get("sanPham").get("id"), idSanPham));
            }
            if (idMauSac != null && idMauSac > 0) {
                predicates.add(cb.equal(root.get("mauSac").get("id"), idMauSac));
            }
            if (idKichThuoc != null && idKichThuoc > 0) {
                predicates.add(cb.equal(root.get("kichThuoc").get("id"), idKichThuoc));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("giaBan"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("giaBan"), maxPrice));
            }
            if (trangThai != null && !trangThai.isEmpty()) {
                predicates.add(cb.equal(root.get("trangThai"), trangThai));
            }

            if (idThuongHieu != null && idThuongHieu > 0) {
                predicates.add(cb.equal(root.get("sanPham").get("thuongHieu").get("id"), idThuongHieu));
            }
            if (idDanhMuc != null && idDanhMuc > 0) {
                predicates.add(cb.equal(root.get("sanPham").get("danhMuc").get("id"), idDanhMuc));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @Override
    public void toggleVariantStatus(Long id) {
        repository.findById(id).ifPresent(v -> {
            String current = v.getTrangThai();
            v.setTrangThai("1".equals(current) ? "0" : "1");
            repository.save(v);
        });
    }

    @Override
    public SanPhamChiTiet findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<SanPhamChiTiet> findBySanPhamId(Long id) {
        return repository.findBySanPhamId(id);
    }

    @Override
    public java.io.ByteArrayInputStream exportToExcel(String search, Long idSanPham, Long idMauSac, Long idKichThuoc, BigDecimal minPrice, BigDecimal maxPrice, String trangThai) {
        org.springframework.data.jpa.domain.Specification<SanPhamChiTiet> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("maSanPhamChiTiet")), keyword),
                    cb.like(cb.lower(root.get("sanPham").get("tenSanPham")), keyword),
                    cb.like(cb.lower(root.get("sanPham").get("maSanPham")), keyword)
                ));
            }
            if (idSanPham != null) predicates.add(cb.equal(root.get("sanPham").get("id"), idSanPham));
            if (idMauSac != null) predicates.add(cb.equal(root.get("mauSac").get("id"), idMauSac));
            if (idKichThuoc != null) predicates.add(cb.equal(root.get("kichThuoc").get("id"), idKichThuoc));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("giaBan"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("giaBan"), maxPrice));
            if (trangThai != null && !trangThai.isEmpty()) predicates.add(cb.equal(root.get("trangThai"), trangThai));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        java.util.List<SanPhamChiTiet> dataList = repository.findAll(spec);
        String[] columns = {"STT", "Mã SP", "Mã CTSP", "Tên sản phẩm", "Màu sắc", "Kích thước", "SL tồn", "Giá bán", "Trạng thái"};
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");

        return com.example.datn_shop_ecom.util.ExcelUtil.exportToExcel("Biến thể sản phẩm", columns, dataList, (row, v) -> {
            row.createCell(0).setCellValue(row.getRowNum());
            row.createCell(1).setCellValue(v.getSanPham() != null ? v.getSanPham().getMaSanPham() : "N/A");
            row.createCell(2).setCellValue(v.getMaSanPhamChiTiet());
            row.createCell(3).setCellValue(v.getSanPham() != null ? v.getSanPham().getTenSanPham() : "N/A");
            row.createCell(4).setCellValue(v.getMauSac() != null ? v.getMauSac().getTenMauSac() : "N/A");
            row.createCell(5).setCellValue(v.getKichThuoc() != null ? v.getKichThuoc().getTenKichThuoc() : "N/A");
            row.createCell(6).setCellValue(v.getSoTonKho() != null ? v.getSoTonKho() : 0);
            row.createCell(7).setCellValue(v.getGiaBan() != null ? df.format(v.getGiaBan()) + " ₫" : "0 ₫");
            row.createCell(8).setCellValue("1".equals(v.getTrangThai()) ? "Đang kinh doanh" : "Ngừng kinh doanh");
        });
    }
}

