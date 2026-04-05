package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "co_giay")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoGiay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenCoGiay;
    private String nguoiTao;
    private LocalDateTime ngayTao;
    private String nguoiSuaCuoi;
    private LocalDateTime ngaySuaCuoi;
    private Boolean xoaMem;
}
