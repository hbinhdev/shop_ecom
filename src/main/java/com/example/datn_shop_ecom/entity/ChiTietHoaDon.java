package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet")
    private SanPhamChiTiet sanPhamChiTiet;

    private BigDecimal gia;

    @Column(name = "gia_nguyen_ban")
    private BigDecimal giaNguyenBan;

    private BigDecimal giamGia;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    private String trangThai;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_sua_cuoi", length = 50)
    private String nguoiSuaCuoi;
}

