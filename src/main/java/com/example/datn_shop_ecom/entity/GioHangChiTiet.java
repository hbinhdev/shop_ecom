package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_gio_hang") // Khớp với tên bảng trong SQL của bạn
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_gio_hang", nullable = false)
    private GioHang gioHang;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    // CÁC CỘT AUDIT KHỚP VỚI SQL CỦA BẠN
    @Column(name = "nguoi_tao", length = 100)
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_sua_cuoi", length = 100)
    private String nguoiSuaCuoi;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "xoa_mem")
    @Builder.Default
    private Boolean xoaMem = false;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        xoaMem = false;
    }

    @PreUpdate
    protected void onUpdate() {
        ngaySuaCuoi = LocalDateTime.now();
    }
}
