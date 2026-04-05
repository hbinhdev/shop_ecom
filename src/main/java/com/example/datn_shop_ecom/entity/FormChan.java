package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "form_chan")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormChan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenForm;
    private String nguoiTao;
    private LocalDateTime ngayTao;
    private String nguoiSuaCuoi;
    private LocalDateTime ngaySuaCuoi;
    private Boolean xoaMem;
}
