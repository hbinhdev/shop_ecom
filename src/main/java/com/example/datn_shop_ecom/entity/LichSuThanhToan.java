package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_thanh_toan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LichSuThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    private String loai;
    private BigDecimal soTienGiaoDich;
    @Column(columnDefinition = "TEXT")
    private String moTa;
    private String trangThai;
    private String phuongThucThanhToan;
    private String nguoiTao;
    private LocalDateTime ngayTao;
    private String nguoiSuaCuoi;
    private LocalDateTime ngaySuaCuoi;
}

