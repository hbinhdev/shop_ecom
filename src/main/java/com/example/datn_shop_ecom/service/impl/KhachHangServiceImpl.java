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
        if (khachHang.getId() != null) {
            // Update mode: Merge with existing data
            KhachHang existing = khachHangRepository.findById(khachHang.getId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
            
            existing.setTenDayDu(khachHang.getTenDayDu());
            existing.setEmail(khachHang.getEmail());
            existing.setSoDienThoai(khachHang.getSoDienThoai());
            
            // Safety check: chỉ cập nhật nếu form có gửi dữ liệu về giới tính/ngày sinh
            if (khachHang.getGioiTinh() != null && !khachHang.getGioiTinh().trim().isEmpty()) {
                existing.setGioiTinh(khachHang.getGioiTinh());
            }
            if (khachHang.getNgaySinh() != null) {
                existing.setNgaySinh(khachHang.getNgaySinh());
            }
            
            existing.setNgaySuaCuoi(LocalDateTime.now());
            existing.setNguoiSuaCuoi("Admin"); // Có thể lấy từ SecurityContext sau
            
            return khachHangRepository.save(existing);
        } else {
            // Create mode
            if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
                khachHang.setMaKhachHang(generateMaKhachHang());
            }
            if (khachHang.getNgayTao() == null) {
                khachHang.setNgayTao(LocalDateTime.now());
            }
            khachHang.setXoaMem(false); // Mặc định hoạt động
            
            return khachHangRepository.save(khachHang);
        }
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
    @Transactional
    public void toggleStatus(Long id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        
        khachHangRepository.updateStatus(id, !khachHang.getXoaMem());
    }
}
