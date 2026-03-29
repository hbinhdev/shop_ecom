package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Column(name = "ten_nguoi_nhan", columnDefinition = "NVARCHAR(255)")
    private String tenNguoiNhan;

    @Column(name = "so_dien_thoai_nguoi_nhan", length = 20)
    private String soDienThoaiNguoiNhan;

    @Column(name = "chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String chiTiet;

    @Column(name = "xa_phuong", columnDefinition = "NVARCHAR(100)")
    private String xaPhuong;

    @Column(name = "quan_huyen", columnDefinition = "NVARCHAR(100)")
    private String quanHuyen;

    @Column(name = "tinh_thanh_pho", columnDefinition = "NVARCHAR(100)")
    private String tinhThanhPho;

    @Column(name = "dia_chi_mac_dinh")
    private Boolean diaChiMacDinh = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "xoa_mem")
    private Boolean xoaMem = false;
}
