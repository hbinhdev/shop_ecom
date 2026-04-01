package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trang_thai_hoa_don")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrangThaiHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_trang_thai", columnDefinition = "NVARCHAR(100)")
    private String tenTrangThai;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;
}
