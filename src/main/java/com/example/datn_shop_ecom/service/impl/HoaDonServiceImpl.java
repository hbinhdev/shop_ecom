package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.entity.ChiTietHoaDon;
import com.example.datn_shop_ecom.repository.HoaDonRepository;
import com.example.datn_shop_ecom.service.HoaDonService;
import com.example.datn_shop_ecom.service.EmailService;
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
    private com.example.datn_shop_ecom.repository.ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.SanPhamChiTietRepository spctRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.TrangThaiHoaDonRepository trangThaiHoaDonRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private com.example.datn_shop_ecom.repository.LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired(required = false)
    private EmailService emailService;

    @Override
    @Transactional
    public HoaDon updateTrangThai(Long id, String status, String note, String user) {
        HoaDon hd = hoaDonRepository.findById(id).orElse(null);
        if (hd == null)
            return null;

        String currentStatus = hd.getTrangThaiHoaDon();

        // Logic trừ tồn kho khi xác nhận đơn hàng
        // Chú ý: "2" hoặc "DA_XAC_NHAN" là trạng thái xác nhận
        if (("2".equals(status) || "DA_XAC_NHAN".equals(status)) &&
                ("1".equals(currentStatus) || "CHO_XAC_NHAN".equals(currentStatus))) {

            List<ChiTietHoaDon> details = chiTietHoaDonRepository.findByHoaDonId(id);
            for (ChiTietHoaDon d : details) {
                com.example.datn_shop_ecom.entity.SanPhamChiTiet spct = d.getSanPhamChiTiet();
                if (spct != null) {
                    spct.setSoTonKho(spct.getSoTonKho() - d.getSoLuong());
                    spctRepository.save(spct);
                }
            }
        }

        hd.setTrangThaiHoaDon(status);
        hd.setNgaySuaCuoi(LocalDateTime.now());
        hd.setNguoiSuaCuoi(user);
        HoaDon saved = hoaDonRepository.save(hd);

        // Lưu lịch sử
        com.example.datn_shop_ecom.entity.LichSuHoaDon history = new com.example.datn_shop_ecom.entity.LichSuHoaDon();
        history.setHoaDon(saved);
        history.setMoTa(note != null ? note : "Cập nhật trạng thái đơn hàngg");
        history.setNguoiTao(user);
        history.setNgayTao(LocalDateTime.now());

        // Cố gắng tìm TrangThaiHoaDon entity tương ứng để set
        try {
            Long statusId = Long.parseLong(status);
            trangThaiHoaDonRepository.findById(statusId).ifPresent(history::setTrangThaiMoi);
        } catch (Exception e) {
            // Nếu status không phải là số (là string CHO_XAC_NHAN), có thể tìm theo mã hoặc
            // bỏ qua
        }

        lichSuHoaDonRepository.save(history);

        return saved;
    }

    @Override
    public List<HoaDon> findAllMatchingInvoices(String maHoaDon, String tenKhachHang, Integer trangThai,
            Integer loaiHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        return hoaDonRepository.findAll((Specification<HoaDon>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (maHoaDon != null && !maHoaDon.isEmpty())
                predicates.add(criteriaBuilder.like(root.get("maHoaDon"), "%" + maHoaDon + "%"));
            if (tenKhachHang != null && !tenKhachHang.isEmpty())
                predicates.add(criteriaBuilder.like(root.get("khachHang").get("tenDayDu"), "%" + tenKhachHang + "%"));
            if (trangThai != null) {
                String statusStr = switch (trangThai) {
                    case 1 -> "CHO_XAC_NHAN";
                    case 2 -> "DA_XAC_NHAN";
                    case 3 -> "DANG_GIAO";
                    case 4 -> "DA_GIAO_HANG";
                    case 5 -> "HOAN_THANH";
                    case 6 -> "DA_HUY";
                    case 7 -> "YEU_CAU_HUY";
                    case 8 -> "CAN_HOAN_PHI";
                    default -> trangThai.toString();
                };
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("trangThaiHoaDon"), statusStr),
                        criteriaBuilder.equal(root.get("trangThaiHoaDon"), trangThai.toString())));
            }
            if (loaiHoaDon != null) {
                String loaiStr = loaiHoaDon == 1 ? "TAI_CUA_HANG"
                        : (loaiHoaDon == 2 ? "GIAO_HANG" : loaiHoaDon.toString());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiStr),
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString())));
            }
            if (ngayBatDau != null)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayTao"), ngayBatDau.atStartOfDay()));
            if (ngayKetThuc != null)
                predicates
                        .add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayTao"), ngayKetThuc.atTime(LocalTime.MAX)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    @Override
    public Page<HoaDon> searchInvoices(String maHoaDon, String tenKhachHang, Integer trangThai, Integer loaiHoaDon,
            LocalDate ngayBatDau, LocalDate ngayKetThuc, Pageable pageable) {
        return hoaDonRepository.findAll((Specification<HoaDon>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (maHoaDon != null && !maHoaDon.isEmpty())
                predicates.add(criteriaBuilder.like(root.get("maHoaDon"), "%" + maHoaDon + "%"));
            if (tenKhachHang != null && !tenKhachHang.isEmpty())
                predicates.add(criteriaBuilder.like(root.get("khachHang").get("tenDayDu"), "%" + tenKhachHang + "%"));
            if (trangThai != null) {
                String statusStr = switch (trangThai) {
                    case 1 -> "CHO_XAC_NHAN";
                    case 2 -> "DA_XAC_NHAN";
                    case 3 -> "DANG_GIAO";
                    case 4 -> "DA_GIAO_HANG";
                    case 5 -> "HOAN_THANH";
                    case 6 -> "DA_HUY";
                    case 7 -> "YEU_CAU_HUY";
                    case 8 -> "CAN_HOAN_PHI";
                    default -> trangThai.toString();
                };
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("trangThaiHoaDon"), statusStr),
                        criteriaBuilder.equal(root.get("trangThaiHoaDon"), trangThai.toString())));
            }
            if (loaiHoaDon != null) {
                String loaiStr = loaiHoaDon == 1 ? "TAI_CUA_HANG"
                        : (loaiHoaDon == 2 ? "GIAO_HANG" : loaiHoaDon.toString());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiStr),
                        criteriaBuilder.equal(root.get("loaiHoaDon"), loaiHoaDon.toString())));
            }
            if (ngayBatDau != null)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayTao"), ngayBatDau.atStartOfDay()));
            if (ngayKetThuc != null)
                predicates
                        .add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayTao"), ngayKetThuc.atTime(LocalTime.MAX)));
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

    // ===== CLIENT-SIDE METHODS =====

    @Override
    @Transactional
    public HoaDon createHoaDonOnline(HoaDon hoaDon, List<ChiTietHoaDon> items) {
        if (hoaDon.getMaHoaDon() == null || hoaDon.getMaHoaDon().isEmpty()) {
            hoaDon.setMaHoaDon(generateMaHoaDon());
        }
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setNgayDatHang(LocalDateTime.now());
        hoaDon.setTrangThaiHoaDon("CHO_XAC_NHAN");
        hoaDon.setLoaiHoaDon("GIAO_HANG");
        hoaDon.setPhuongThucThanhToan("TIEN_MAT");
        HoaDon saved = hoaDonRepository.save(hoaDon);
        if (items != null) {
            for (ChiTietHoaDon item : items) {
                item.setHoaDon(saved);
                item.setNgayTao(LocalDateTime.now());
                chiTietHoaDonRepository.save(item);
            }
        }
        // Send confirmation email
        try {
            if (emailService != null && saved.getKhachHang() != null) {
                String subject = "Dat hang thanh cong tai PeakSneaker - " + saved.getMaHoaDon();
                String body = "Chao " + saved.getKhachHang().getTenDayDu() + "!\n\n" +
                        "Don hang " + saved.getMaHoaDon() + " da duoc dat thanh cong.\n" +
                        "Trang thai: Cho xac nhan\n" +
                        "Tong tien: " + (saved.getTongTien() != null ? saved.getTongTien().toPlainString() : "0")
                        + " VND\n" +
                        "Dia chi giao: " + saved.getChiTietNguoiNhan() + ", " + saved.getXaPhuong() + ", "
                        + saved.getQuanHuyen() + ", " + saved.getTinhThanhPho() + "\n\n" +
                        "Cam on ban da tin tuong PeakSneaker!";
                emailService.sendEmail(saved.getKhachHang().getEmail(), subject, body);
            }
        } catch (Exception ignored) {
        }
        return saved;
    }

    @Override
    public HoaDon findByMaHoaDon(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon).orElse(null);
    }

    @Override
    public List<HoaDon> findByKhachHangEmail(String email) {
        return hoaDonRepository.findByKhachHangEmailOrderByNgayTaoDesc(email);
    }

    @Override
    public String generateMaHoaDon() {
        long count = hoaDonRepository.count();
        return String.format("HD%08d", count + 1);
    }
}
