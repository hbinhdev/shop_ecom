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

    @Column(name = "ten_day_du", columnDefinition = "NVARCHAR(255)")
    private String tenDayDu;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", columnDefinition = "NVARCHAR(20)")
    private String gioiTinh;

    @Column(length = 100)
    private String email;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(length = 255)
    private String matKhau;

    @ManyToOne
    @JoinColumn(name = "id_vai_tro")
    private VaiTro vaiTro;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String trangThai;

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

    @Column(name = "diem_tich_luy")
    private Integer diemTichLuy = 0;

    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DiaChi> danhSachDiaChi = new java.util.ArrayList<>();
}
