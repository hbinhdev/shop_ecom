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

    @Autowired
    private com.example.datn_shop_ecom.repository.ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.LichSuThanhToanRepository lichSuThanhToanRepository;

    @Override
    public List<HoaDon> findAllMatchingInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        return hoaDonRepository.findAll((Specification<HoaDon>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (maHoaDon != null && !maHoaDon.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("maHoaDon"), "%" + maHoaDon + "%"));
            }

            if (tenKhachHang != null && !tenKhachHang.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("khachHang").get("tenDayDu"), "%" + tenKhachHang + "%"));
            }

            if (trangThai != null) {
                predicates.add(criteriaBuilder.equal(root.get("trangThaiHoaDon"), trangThai.toString()));
            }

            if (loaiHoaDon != null) {
                String loaiStr = loaiHoaDon == 1 ? "TAI_CUA_HANG" : (loaiHoaDon == 2 ? "GIAO_HANG" : loaiHoaDon.toString());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiStr),
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString())
                ));
            }

            if (ngayBatDau != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayTao"), ngayBatDau.atStartOfDay()));
            }

            if (ngayKetThuc != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayTao"), ngayKetThuc.atTime(LocalTime.MAX)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    @Override
    public Page<HoaDon> searchInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc, Pageable pageable) {
        return hoaDonRepository.findAll((Specification<HoaDon>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (maHoaDon != null && !maHoaDon.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("maHoaDon"), "%" + maHoaDon + "%"));
            }

            if (tenKhachHang != null && !tenKhachHang.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("khachHang").get("tenDayDu"), "%" + tenKhachHang + "%"));
            }

            if (trangThai != null) {

                predicates.add(criteriaBuilder.equal(root.get("trangThaiHoaDon"), trangThai.toString()));
            }

            if (loaiHoaDon != null) {
                String loaiStr = loaiHoaDon == 1 ? "TAI_CUA_HANG" : (loaiHoaDon == 2 ? "GIAO_HANG" : loaiHoaDon.toString());
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("loaiHoaDon"), loaiStr),
                    criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString())
                ));
            }

            if (ngayBatDau != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayTao"), ngayBatDau.atStartOfDay()));
            }

            if (ngayKetThuc != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayTao"), ngayKetThuc.atTime(LocalTime.MAX)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    @Override
    public HoaDon findById(Long id) {
        return hoaDonRepository.findById(id).orElse(null);
    }

    @Override
    public List<com.example.datn_shop_ecom.entity.ChiTietHoaDon> findDetailByHoaDonId(Long hoaDonId) {
        return chiTietHoaDonRepository.findByHoaDonId(hoaDonId);
    }

    @Override
    public List<com.example.datn_shop_ecom.entity.LichSuHoaDon> findHistoryByHoaDonId(Long hoaDonId) {
        return lichSuHoaDonRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDonId);
    }

    @Override
    public List<com.example.datn_shop_ecom.entity.LichSuThanhToan> findPaymentHistoryByHoaDonId(Long hoaDonId) {
        return lichSuThanhToanRepository.findByHoaDonIdOrderByNgayTaoDesc(hoaDonId);
    }
}

