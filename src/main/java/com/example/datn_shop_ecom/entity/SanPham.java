package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private String duongDanAnh;
    @Column(columnDefinition = "TEXT")
    private String moTa;
    private String nguoiTao;
    private LocalDateTime ngayTao;
    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "xoa_mem")
    private Boolean xoaMem;

    @OneToMany(mappedBy = "sanPham")
    private java.util.List<HinhAnh> danhSachHinhAnh;
}
