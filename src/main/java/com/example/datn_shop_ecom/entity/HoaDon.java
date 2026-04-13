package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "ma_hoa_don", nullable = false, length = 50)
    private String maHoaDon;

    @Column(name = "loai_hoa_don", columnDefinition = "NVARCHAR(100)")
    private String loaiHoaDon;

    @Column(name = "hinh_thuc_hoa_don", columnDefinition = "NVARCHAR(100)")
    private String hinhThucHoaDon;

    @Column(name = "phuong_thuc_thanh_toan", columnDefinition = "NVARCHAR(100)")
    private String phuongThucThanhToan;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "tong_tien_after_giam")
    private BigDecimal tongTienAfterGiam;

    @Column(name = "tien_van_chuyen")
    private BigDecimal tienVanChuyen;

    @Column(name = "tien_phieu_giam_gia")
    private BigDecimal tienPhieuGiamGia;

    @Column(name = "ngay_dat_hang")
    private LocalDateTime ngayDatHang;

    @Column(name = "trang_thai_hoa_don")
    private String trangThaiHoaDon;

    @ManyToOne
    @JoinColumn(name = "id_trang_thai_moi")
    private TrangThaiHoaDon trangThaiMoi;

    @Column(name = "id_phieu_giam_gia")
    private Long idPhieuGiamGia;

    @Column(name = "ten_nguoi_nhan", columnDefinition = "NVARCHAR(255)")
    private String tenNguoiNhan;

    @Column(name = "so_dien_thoai_nguoi_nhan", length = 15)
    private String soDienThoaiNguoiNhan;

    @Column(name = "chi_tiet_nguoi_nhan", columnDefinition = "NVARCHAR(MAX)")
    private String chiTietNguoiNhan;

    @Column(name = "xa_phuong", columnDefinition = "NVARCHAR(255)")
    private String xaPhuong;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(255)")
    private String quanHuyen;

    @Column(name = "tinh_thanh_pho", columnDefinition = "NVARCHAR(255)")
    private String tinhThanhPho;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "nguoi_sua_cuoi", length = 50)
    private String nguoiSuaCuoi;
}

