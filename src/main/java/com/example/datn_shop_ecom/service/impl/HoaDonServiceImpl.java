package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.entity.TrangThaiHoaDon;
import com.example.datn_shop_ecom.repository.HoaDonRepository;
import com.example.datn_shop_ecom.repository.NhanVienRepository;
import com.example.datn_shop_ecom.repository.TrangThaiHoaDonRepository;
import com.example.datn_shop_ecom.service.HoaDonService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private TrangThaiHoaDonRepository trangThaiHoaDonRepository;

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
                List<String> statuses = mapTrangThai(trangThai);
                predicates.add(root.get("trangThaiHoaDon").in(statuses));
            }

            if (loaiHoaDon != null) {
                if (loaiHoaDon == 1) {
                    predicates.add(root.get("loaiHoaDon").in(List.of("1", "TAI_CUA_HANG", "TAI_QUAY")));
                } else if (loaiHoaDon == 2) {
                    predicates.add(root.get("loaiHoaDon").in(List.of("2", "GIAO_HANG")));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString()));
                }
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

    /** Map số tab → danh sách giá trị trangThaiHoaDon tương ứng (cả số và string) */
    private static List<String> mapTrangThai(Integer trangThai) {
        if (trangThai == null) return List.of();
        return switch (trangThai) {
            case 1  -> List.of("1", "CHO_XAC_NHAN");
            case 2  -> List.of("2", "DA_XAC_NHAN");
            case 3  -> List.of("3", "DANG_GIAO");
            case 4  -> List.of("4");
            case 5  -> List.of("4", "HOAN_THANH");
            case 6  -> List.of("5", "DA_HUY");
            default -> List.of(trangThai.toString());
        };
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
                List<String> statuses = mapTrangThai(trangThai);
                predicates.add(root.get("trangThaiHoaDon").in(statuses));
            }

            if (loaiHoaDon != null) {
                if (loaiHoaDon == 1) {
                    predicates.add(root.get("loaiHoaDon").in(List.of("1", "TAI_CUA_HANG", "TAI_QUAY")));
                } else if (loaiHoaDon == 2) {
                    predicates.add(root.get("loaiHoaDon").in(List.of("2", "GIAO_HANG")));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString()));
                }
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

    @Override
    @Transactional
    public HoaDon createPendingInvoice(String nhanVienEmail) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(nhanVienEmail).orElse(null);

        // Sinh mã hóa đơn: HD + 4 chữ số, đảm bảo không trùng
        long nextId = hoaDonRepository.findMaxId() + 1;
        String maHoaDon;
        do {
            maHoaDon = String.format("HD%04d", nextId++);
        } while (hoaDonRepository.existsByMaHoaDon(maHoaDon));

        // Lấy trạng thái "Chờ thanh toán", nếu chưa có thì tạo mới
        TrangThaiHoaDon trangThai = trangThaiHoaDonRepository
                .findByTenTrangThai("Chờ thanh toán")
                .orElseGet(() -> trangThaiHoaDonRepository.save(
                        TrangThaiHoaDon.builder()
                                .tenTrangThai("Chờ thanh toán")
                                .moTa("Hóa đơn tại quầy đang chờ thanh toán")
                                .build()
                ));

        HoaDon hoaDon = HoaDon.builder()
                .maHoaDon(maHoaDon)
                .loaiHoaDon("TAI_QUAY")
                .trangThaiHoaDon("CHO_THANH_TOAN")
                .trangThaiMoi(trangThai)
                .nhanVien(nhanVien)
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nhanVienEmail)
                .build();

        return hoaDonRepository.save(hoaDon);
    }

    @Override
    public List<HoaDon> findAllPendingPOS() {
        return hoaDonRepository.findAllPendingPOS();
    }
}
