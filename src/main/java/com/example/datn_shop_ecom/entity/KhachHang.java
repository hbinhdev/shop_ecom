package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_khach_hang", nullable = false, length = 50)
    private String maKhachHang;

    @Column(name = "ten_day_du")
    private String tenDayDu;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh")
    private String gioiTinh;

    @Column(length = 100)
    private String email;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(length = 255)
    private String matKhau;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "nguoi_sua_cuoi", length = 50)
    private String nguoiSuaCuoi;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "xoa_mem")
    private Boolean xoaMem = false;
}
