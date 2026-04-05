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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuong_hieu")
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_xuat_xu")
    private XuatXu xuatXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_co_giay")
    private CoGiay coGiay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vi_tri")
    private ViTri viTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phong_cach")
    private PhongCachChoi phongCachChoi;

    @OneToMany(mappedBy = "sanPham")
    private java.util.List<HinhAnh> danhSachHinhAnh;

    @OneToMany(mappedBy = "sanPham", fetch = FetchType.LAZY)
    private java.util.List<SanPhamChiTiet> danhSachChiTiet;

    @Transient
    public Integer getTongTonKho() {
        if(danhSachChiTiet == null || danhSachChiTiet.isEmpty()) return 0;
        return danhSachChiTiet.stream().filter(c -> c.getSoTonKho() != null).mapToInt(SanPhamChiTiet::getSoTonKho).sum();
    }

    @Transient
    public String getKhoangGia() {
        if(danhSachChiTiet == null || danhSachChiTiet.isEmpty()) return "0 đ";
        java.math.BigDecimal min = danhSachChiTiet.stream().filter(c -> c.getGiaBan() != null).map(SanPhamChiTiet::getGiaBan).min(java.math.BigDecimal::compareTo).orElse(java.math.BigDecimal.ZERO);
        java.math.BigDecimal max = danhSachChiTiet.stream().filter(c -> c.getGiaBan() != null).map(SanPhamChiTiet::getGiaBan).max(java.math.BigDecimal::compareTo).orElse(java.math.BigDecimal.ZERO);
        if(min.compareTo(max) == 0) return String.format("%,.0f đ", min);
        return String.format("%,.0f đ - %,.0f đ", min, max);
    }
}
