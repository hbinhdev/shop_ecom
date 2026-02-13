package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_vai_tro")
    private VaiTro vaiTro;

    @Column(name = "ma_nhan_vien", nullable = false, length = 50)
    private String maNhanVien;

    @Column(name = "ten_day_du", columnDefinition = "NVARCHAR(255)")
    private String tenDayDu;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", columnDefinition = "NVARCHAR(10)")
    private String gioiTinh;

    @Column(name = "dia_chi", columnDefinition = "NVARCHAR(MAX)")
    private String diaChi;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String matKhau;

    @Column(name = "xa_phuong", columnDefinition = "NVARCHAR(100)")
    private String xaPhuong;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(100)")
    private String quanHuyen;

    @Column(name = "tinh_thanh_pho", columnDefinition = "NVARCHAR(100)")
    private String tinhThanhPho;

    private Integer trangThai;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_sua_cuoi", length = 50)
    private String nguoiSuaCuoi;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;
}
