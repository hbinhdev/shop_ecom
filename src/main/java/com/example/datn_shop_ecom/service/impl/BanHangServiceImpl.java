package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import com.example.datn_shop_ecom.service.BanHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BanHangServiceImpl implements BanHangService {

    @Autowired private SanPhamChiTietRepository spctRepo;
    @Autowired private ChiTietHoaDonRepository cthdRepo;
    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private KhachHangRepository khachHangRepo;
    @Autowired private VaiTroRepository vaiTroRepo;
    @Autowired private PhieuGiamGiaRepository phieuGiamGiaRepo;
    @Autowired private LichSuThanhToanRepository lichSuThanhToanRepo;
    @Autowired private TrangThaiHoaDonRepository trangThaiHoaDonRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------
    @Override
    public List<SanPhamChiTiet> searchProducts(String keyword) {
        return spctRepo.searchForPOS(keyword, PageRequest.of(0, 500));
    }

    @Override
    public SanPhamChiTiet findByBarcode(String maVach) {
        return spctRepo.findByMaVach(maVach).orElse(null);
    }

    // ----------------------------------------------------------------
    @Override
    @Transactional
    public ChiTietHoaDon addProduct(Long hoaDonId, Long spctId, int soLuong, String nguoiThuc) {
        HoaDon hoaDon = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID=" + hoaDonId));

        if (!"CHO_THANH_TOAN".equals(hoaDon.getTrangThaiHoaDon())) {
            throw new IllegalStateException("Hóa đơn không ở trạng thái chờ thanh toán");
        }

        SanPhamChiTiet spct = spctRepo.findById(spctId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID=" + spctId));

        if (spct.getSoTonKho() == null || spct.getSoTonKho() < soLuong) {
            throw new IllegalStateException("Tồn kho không đủ. Còn lại: " + (spct.getSoTonKho() == null ? 0 : spct.getSoTonKho()));
        }

        // Trừ tồn kho an toàn (WHERE soTonKho >= soLuong)
        int updated = spctRepo.reduceStock(spctId, soLuong);
        if (updated == 0) {
            throw new IllegalStateException("Tồn kho không đủ hoặc đã bị thay đổi. Vui lòng thử lại.");
        }

        // Nếu sản phẩm đã có trong giỏ → cập nhật số lượng
        ChiTietHoaDon chiTiet = cthdRepo
                .findByHoaDonIdAndSanPhamChiTietId(hoaDonId, spctId)
                .orElse(null);

        if (chiTiet != null) {
            int newQty = chiTiet.getSoLuong() + soLuong;
            chiTiet.setSoLuong(newQty);
            chiTiet.setThanhTien(chiTiet.getGia().multiply(BigDecimal.valueOf(newQty)));
            chiTiet.setNguoiSuaCuoi(nguoiThuc);
            chiTiet.setNgaySuaCuoi(LocalDateTime.now());
        } else {
            BigDecimal gia = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
            chiTiet = ChiTietHoaDon.builder()
                    .hoaDon(hoaDon)
                    .sanPhamChiTiet(spct)
                    .gia(gia)
                    .giaNguyenBan(gia)
                    .soLuong(soLuong)
                    .thanhTien(gia.multiply(BigDecimal.valueOf(soLuong)))
                    .trangThai("1")
                    .nguoiTao(nguoiThuc)
                    .ngayTao(LocalDateTime.now())
                    .build();
        }

        chiTiet = cthdRepo.save(chiTiet);

        // Cập nhật tổng tiền hóa đơn
        recalcHoaDon(hoaDon);

        return chiTiet;
    }

    // ----------------------------------------------------------------
    @Override
    @Transactional
    public ChiTietHoaDon updateQuantity(Long chiTietId, int soLuongMoi, String nguoiThuc) {
        if (soLuongMoi <= 0) throw new IllegalArgumentException("Số lượng phải lớn hơn 0");

        ChiTietHoaDon chiTiet = cthdRepo.findById(chiTietId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng chi tiết ID=" + chiTietId));

        int delta = soLuongMoi - chiTiet.getSoLuong();

        if (delta > 0) {
            // Cần thêm hàng → trừ tồn
            int updated = spctRepo.reduceStock(chiTiet.getSanPhamChiTiet().getId(), delta);
            if (updated == 0) {
                SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
                throw new IllegalStateException("Tồn kho không đủ. Còn lại: " + spct.getSoTonKho());
            }
        } else if (delta < 0) {
            // Giảm hàng → hoàn trả tồn
            spctRepo.restoreStock(chiTiet.getSanPhamChiTiet().getId(), -delta);
        }

        chiTiet.setSoLuong(soLuongMoi);
        chiTiet.setThanhTien(chiTiet.getGia().multiply(BigDecimal.valueOf(soLuongMoi)));
        chiTiet.setNguoiSuaCuoi(nguoiThuc);
        chiTiet.setNgaySuaCuoi(LocalDateTime.now());
        chiTiet = cthdRepo.save(chiTiet);

        recalcHoaDon(chiTiet.getHoaDon());
        return chiTiet;
    }

    // ----------------------------------------------------------------
    @Override
    @Transactional
    public void removeItem(Long chiTietId, String nguoiThuc) {
        ChiTietHoaDon chiTiet = cthdRepo.findById(chiTietId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng chi tiết ID=" + chiTietId));

        // Hoàn trả tồn kho
        spctRepo.restoreStock(chiTiet.getSanPhamChiTiet().getId(), chiTiet.getSoLuong());

        HoaDon hoaDon = chiTiet.getHoaDon();
        cthdRepo.delete(chiTiet);

        recalcHoaDon(hoaDon);
    }

    // ----------------------------------------------------------------
    @Override
    public List<ChiTietHoaDon> getCartItems(Long hoaDonId) {
        return cthdRepo.findByHoaDonId(hoaDonId);
    }

    // ----------------------------------------------------------------
    /** Tính lại tongTien cho hóa đơn sau mỗi thay đổi */
    private void recalcHoaDon(HoaDon hoaDon) {
        BigDecimal tong = cthdRepo.sumThanhTienByHoaDonId(hoaDon.getId());
        hoaDon.setTongTien(tong);
        // Nếu đã có phiếu giảm giá → tính lại tongTienAfterGiam và tienPhieuGiamGia
        if (hoaDon.getPhieuGiamGia() != null) {
            BigDecimal afterGiam = calcAfterDiscount(tong, hoaDon.getPhieuGiamGia());
            hoaDon.setTongTienAfterGiam(afterGiam);
            hoaDon.setTienPhieuGiamGia(tong.subtract(afterGiam));
        } else {
            hoaDon.setTongTienAfterGiam(tong);
            hoaDon.setTienPhieuGiamGia(BigDecimal.ZERO);
        }
        hoaDon.setNgaySuaCuoi(LocalDateTime.now());
        hoaDonRepo.save(hoaDon);
    }

    /** Tính giá sau giảm */
    private BigDecimal calcAfterDiscount(BigDecimal tong, PhieuGiamGia p) {
        if (p == null || p.getGiaTriGiam() == null) return tong;
        BigDecimal giam;
        if ("%".equals(p.getHinhThucGiam())) {
            giam = tong.multiply(p.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
        } else {
            giam = p.getGiaTriGiam();
        }
        BigDecimal result = tong.subtract(giam);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    // ================================================================
    //  Task 3: Khách hàng
    // ================================================================
    @Override
    public List<KhachHang> searchCustomers(String keyword) {
        return khachHangRepo.searchForPOS(keyword, PageRequest.of(0, 10));
    }

    @Override
    @Transactional
    public HoaDon assignCustomer(Long hoaDonId, Long khachHangId, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        KhachHang kh = khachHangRepo.findById(khachHangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));
        hd.setKhachHang(kh);
        hd.setNgaySuaCuoi(LocalDateTime.now());
        hd.setNguoiSuaCuoi(nguoiThuc);
        return hoaDonRepo.save(hd);
    }

    @Override
    @Transactional
    public HoaDon removeCustomer(Long hoaDonId, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        hd.setKhachHang(null);
        // Gỡ luôn phiếu giảm giá nếu có
        hd.setPhieuGiamGia(null);
        hd.setTienPhieuGiamGia(null);
        recalcHoaDon(hd);
        hd.setNguoiSuaCuoi(nguoiThuc);
        return hoaDonRepo.save(hd);
    }

    @Override
    @Transactional
    public KhachHang quickCreateCustomer(String tenDayDu, String soDienThoai, String email, String nguoiThuc) {
        if (khachHangRepo.existsBySoDienThoai(soDienThoai)) {
            throw new IllegalArgumentException("Số điện thoại " + soDienThoai + " đã tồn tại trong hệ thống");
        }
        VaiTro vaiTro = vaiTroRepo.findByMa("ROLE_CUSTOMER").orElse(null);
        long count = khachHangRepo.count();
        KhachHang kh = KhachHang.builder()
                .maKhachHang(String.format("KH%05d", count + 1))
                .tenDayDu(tenDayDu)
                .soDienThoai(soDienThoai)
                .email(email)
                .vaiTro(vaiTro)
                .trangThai("Hoạt động")
                .xoaMem(false)
                .diemTichLuy(0)
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nguoiThuc)
                .build();
        // Nếu có email mới set password
        if (email != null && !email.isBlank()) {
            kh.setMatKhau(passwordEncoder.encode("123456"));
        }
        return khachHangRepo.save(kh);
    }

    @Override
    public List<PhieuGiamGia> getApplicableVouchers(BigDecimal tongTien) {
        return phieuGiamGiaRepo.findApplicableVouchers(
                tongTien != null ? tongTien : BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public HoaDon applyVoucher(Long hoaDonId, Long phieuId, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        PhieuGiamGia p = phieuGiamGiaRepo.findById(phieuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        if (p.getSoLuong() != null && p.getSoLuong() <= 0)
            throw new IllegalStateException("Phiếu giảm giá đã hết số lượng");

        BigDecimal tong = hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
        if (p.getGiaTriToiThieu() != null && tong.compareTo(p.getGiaTriToiThieu()) < 0)
            throw new IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu "
                    + String.format("%,.0f ₫", p.getGiaTriToiThieu()));

        BigDecimal afterGiam = calcAfterDiscount(tong, p);
        hd.setPhieuGiamGia(p);
        hd.setTienPhieuGiamGia(tong.subtract(afterGiam));
        hd.setTongTienAfterGiam(afterGiam);
        hd.setNgaySuaCuoi(LocalDateTime.now());
        hd.setNguoiSuaCuoi(nguoiThuc);
        return hoaDonRepo.save(hd);
    }

    @Override
    @Transactional
    public HoaDon removeVoucher(Long hoaDonId, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        hd.setPhieuGiamGia(null);
        hd.setTienPhieuGiamGia(null);
        hd.setTongTienAfterGiam(hd.getTongTien());
        hd.setNgaySuaCuoi(LocalDateTime.now());
        hd.setNguoiSuaCuoi(nguoiThuc);
        return hoaDonRepo.save(hd);
    }

    // ================================================================
    //  Task 4: Thanh toán
    // ================================================================
    @Override
    @Transactional
    public HoaDon checkout(Long hoaDonId, BigDecimal tienKhachDua, String phuongThuc, String loaiDon, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (!"CHO_THANH_TOAN".equals(hd.getTrangThaiHoaDon()))
            throw new IllegalStateException("Hóa đơn không ở trạng thái chờ thanh toán");

        List<ChiTietHoaDon> items = cthdRepo.findByHoaDonId(hoaDonId);
        if (items.isEmpty())
            throw new IllegalStateException("Hóa đơn chưa có sản phẩm");

        BigDecimal tongTienThanhToan = hd.getTongTienAfterGiam() != null
                ? hd.getTongTienAfterGiam() : BigDecimal.ZERO;

        // Tiền mặt: kiểm tra đủ tiền
        if ("TIEN_MAT".equals(phuongThuc)) {
            if (tienKhachDua == null || tienKhachDua.compareTo(tongTienThanhToan) < 0)
                throw new IllegalStateException("Tiền khách đưa không đủ. Cần tối thiểu: "
                        + String.format("%,.0f ₫", tongTienThanhToan));
        } else {
            // Chuyển khoản: tienKhachDua = tổng tiền thanh toán
            tienKhachDua = tongTienThanhToan;
        }

        BigDecimal tienThuaTra = tienKhachDua.subtract(tongTienThanhToan);

        // Xác định loại đơn và trạng thái tương ứng
        boolean isGiaoHang = "GIAO_HANG".equals(loaiDon);
        String loaiHoaDon = isGiaoHang ? "GIAO_HANG" : "TAI_QUAY";
        String trangThaiStr = isGiaoHang ? "DA_XAC_NHAN" : "HOAN_THANH";
        String tenTrangThai = isGiaoHang ? "Đã xác nhận" : "Hoàn thành";
        String moTaTrangThai = isGiaoHang ? "Đơn giao hàng đã được xác nhận" : "Hóa đơn tại quầy đã hoàn thành";

        TrangThaiHoaDon trangThaiMoi = trangThaiHoaDonRepo
                .findByTenTrangThai(tenTrangThai)
                .orElseGet(() -> trangThaiHoaDonRepo.save(
                        TrangThaiHoaDon.builder()
                                .tenTrangThai(tenTrangThai)
                                .moTa(moTaTrangThai)
                                .build()));

        hd.setLoaiHoaDon(loaiHoaDon);
        hd.setTrangThaiHoaDon(trangThaiStr);
        hd.setTrangThaiMoi(trangThaiMoi);
        hd.setPhuongThucThanhToan(phuongThuc);
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTienKhachDua(tienKhachDua);
        hd.setTienThuaTra(tienThuaTra);
        hd.setNguoiSuaCuoi(nguoiThuc);
        hd.setNgaySuaCuoi(LocalDateTime.now());

        // Giảm số lượng phiếu giảm giá
        if (hd.getPhieuGiamGia() != null) {
            PhieuGiamGia p = hd.getPhieuGiamGia();
            if (p.getSoLuong() != null && p.getSoLuong() > 0) {
                p.setSoLuong(p.getSoLuong() - 1);
                phieuGiamGiaRepo.save(p);
            }
        }

        // Tăng điểm tích lũy (1 điểm / 10.000 ₫)
        if (hd.getKhachHang() != null) {
            KhachHang kh = hd.getKhachHang();
            int diemCong = tongTienThanhToan
                    .divide(BigDecimal.valueOf(10000), 0, RoundingMode.DOWN).intValue();
            if (diemCong > 0) {
                kh.setDiemTichLuy((kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0) + diemCong);
                khachHangRepo.save(kh);
            }
        }

        // Ghi lịch sử thanh toán
        lichSuThanhToanRepo.save(LichSuThanhToan.builder()
                .hoaDon(hd)
                .loai("THANH_TOAN")
                .soTienGiaoDich(tongTienThanhToan)
                .phuongThucThanhToan(phuongThuc)
                .moTa("Thanh toán tại quầy - " + hd.getMaHoaDon())
                .trangThai("THANH_CONG")
                .nguoiTao(nguoiThuc)
                .ngayTao(LocalDateTime.now())
                .build());

        return hoaDonRepo.save(hd);
    }

    @Override
    @Transactional
    public void cancelInvoice(Long hoaDonId, String nguoiThuc) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (!"CHO_THANH_TOAN".equals(hd.getTrangThaiHoaDon()))
            throw new IllegalStateException("Không thể hủy hóa đơn này");

        // Hoàn trả tồn kho cho tất cả sản phẩm trong giỏ
        List<ChiTietHoaDon> items = cthdRepo.findByHoaDonId(hoaDonId);
        for (ChiTietHoaDon item : items) {
            if (item.getSoLuong() != null && item.getSoLuong() > 0)
                spctRepo.restoreStock(item.getSanPhamChiTiet().getId(), item.getSoLuong());
        }
        cthdRepo.deleteAll(items);

        TrangThaiHoaDon trangThaiHuy = trangThaiHoaDonRepo
                .findByTenTrangThai("Đã hủy")
                .orElseGet(() -> trangThaiHoaDonRepo.save(
                        TrangThaiHoaDon.builder()
                                .tenTrangThai("Đã hủy")
                                .moTa("Hóa đơn đã bị hủy")
                                .build()));

        hd.setTrangThaiHoaDon("DA_HUY");
        hd.setTrangThaiMoi(trangThaiHuy);
        hd.setNgaySuaCuoi(LocalDateTime.now());
        hd.setNguoiSuaCuoi(nguoiThuc);
        hoaDonRepo.save(hd);
    }
}