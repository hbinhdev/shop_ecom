package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.PhieuGiamGia;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface PhieuGiamGiaService {
    List<PhieuGiamGia> filterPhieuGiamGia(String search, LocalDate startDate, LocalDate endDate, Integer status);
    org.springframework.data.domain.Page<PhieuGiamGia> filterPhieuGiamGiaPage(String search, LocalDate startDate, LocalDate endDate, Integer status, org.springframework.data.domain.Pageable pageable);
    
    PhieuGiamGia savePGG(PhieuGiamGia pgg);
    
    PhieuGiamGia findById(Long id);
    
    void toggleStatus(Long id);
    
    void softDelete(Long id);
    
    String generateMaPGG();
    List<PhieuGiamGia> findAllByXoaMemFalse();
    void updateAllStatuses();
    ByteArrayInputStream exportToExcel(String search, LocalDate startDate, LocalDate endDate, Integer status);
}

