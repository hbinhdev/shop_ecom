package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vai_tro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String ma;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String ten;

    @Column(name = "nguoi_tao", length = 50)
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "nguoi_sua_cuoi", length = 50)
    private String nguoiSuaCuoi;

    @Column(name = "ngay_sua_cuoi")
    private LocalDateTime ngaySuaCuoi;

    @Column(name = "xoa_mem")
    private Boolean xoaMem = false;
}
