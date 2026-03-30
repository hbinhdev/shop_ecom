package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public java.util.List<KhachHang> filterKhachHang(String search, String gioiTinh, Boolean xoaMem) {
        if (search != null && search.trim().isEmpty()) search = null;
        if (gioiTinh != null && gioiTinh.trim().isEmpty()) gioiTinh = null;
        return khachHangRepository.findByFilters(search, gioiTinh, xoaMem);
    }

    @Override
    public java.util.List<KhachHang> getAllKhachHangs() {
        return khachHangRepository.findAllByXoaMemFalse();
    }

    @Override
    @Transactional
    public KhachHang saveKhachHang(KhachHang khachHang) {
        if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
            khachHang.setMaKhachHang(generateMaKhachHang());
        }
        if (khachHang.getNgayTao() == null) {
            khachHang.setNgayTao(LocalDateTime.now());
        }
        
        // Link addresses to customer
        if (khachHang.getDanhSachDiaChi() != null) {
            khachHang.getDanhSachDiaChi().forEach(dc -> {
                dc.setKhachHang(khachHang);
                dc.setNgayTao(LocalDateTime.now());
                if (dc.getDiaChiMacDinh() == null) {
                    dc.setDiaChiMacDinh(false);
                }
            });
        }
        
        return khachHangRepository.save(khachHang);
    }

    @Override
    public String generateMaKhachHang() {
        long count = khachHangRepository.count();
        return String.format("KH%05d", count + 1);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        khachHangRepository.updateStatus(id, !khachHang.getXoaMem());
    }
}
