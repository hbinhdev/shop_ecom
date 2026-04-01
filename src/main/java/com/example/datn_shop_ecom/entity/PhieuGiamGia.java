package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_giam_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", unique = true, nullable = false, length = 50)
    private String maPhieu;

    @Column(name = "ten_phieu", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String tenPhieu;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "hinh_thuc_giam", columnDefinition = "NVARCHAR(50)")
    private String hinhThucGiam; // "%" or "VNĐ"

    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_toi_thieu")
    private BigDecimal giaTriToiThieu;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Ngừng hoạt động, 1: Hoạt động

    @Builder.Default
    @Column(name = "loai")
    private Integer loai = 0; // 0: Công khai, 1: Cá nhân

    @Builder.Default
    @Column(name = "xoa_mem")
    private Boolean xoaMem = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "nguoi_sua_cuoi")
    private String nguoiSuaCuoi;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;
}
