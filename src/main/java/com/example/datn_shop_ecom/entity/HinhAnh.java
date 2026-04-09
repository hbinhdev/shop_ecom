package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hinh_anh")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HinhAnh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet")
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(columnDefinition = "VARCHAR(MAX)")
    private String duongDan;

    @Column(name = "la_anh_dai_dien")
    private Boolean laAnhDaiDien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        if (laAnhDaiDien == null) laAnhDaiDien = false;
    }
}

