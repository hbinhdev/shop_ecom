package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.repository.HoaDonRepository;
import com.example.datn_shop_ecom.service.HoaDonService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Override
    public Page<HoaDon> searchInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayTao, Pageable pageable) {
        return hoaDonRepository.findAll((Specification<HoaDon>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (maHoaDon != null && !maHoaDon.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("maHoaDon"), "%" + maHoaDon + "%"));
            }

            if (tenKhachHang != null && !tenKhachHang.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("khachHang").get("tenDayDu"), "%" + tenKhachHang + "%"));
            }

            if (trangThai != null) {
                predicates.add(criteriaBuilder.equal(root.get("trangThaiHoaDon"), trangThai));
            }

            if (loaiHoaDon != null) {
                predicates.add(criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon));
            }

            if (ngayTao != null) {
                LocalDateTime startOfDay = ngayTao.atStartOfDay();
                LocalDateTime endOfDay = ngayTao.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("ngayTao"), startOfDay, endOfDay));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
