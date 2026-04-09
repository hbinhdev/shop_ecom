package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.NhanVien;
import java.io.ByteArrayInputStream;
import java.util.List;

public interface NhanVienService {
    List<NhanVien> filterNhanVien(String search, Long idVaiTro, Boolean xoaMem);
    org.springframework.data.domain.Page<NhanVien> filterNhanVienPage(String search, Long idVaiTro, Boolean xoaMem, org.springframework.data.domain.Pageable pageable);
    List<NhanVien> getAllNhanViens();
    NhanVien saveNhanVien(NhanVien nhanVien, org.springframework.web.multipart.MultipartFile anhFile);
    NhanVien findById(Long id);
    void toggleStatus(Long id);
    String generateMaNhanVien();
    ByteArrayInputStream exportToExcel(String search, Long idVaiTro, Boolean xoaMem);
}

