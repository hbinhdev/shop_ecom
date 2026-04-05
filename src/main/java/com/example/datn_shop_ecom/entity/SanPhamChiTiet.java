package com.example.datn_shop_ecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham_chi_tiet")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_san")
    private LoaiSan loaiSan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_form_chan")
    private FormChan formChan;

    private String maSanPhamChiTiet;
    private String duongDanAnh;
    private BigDecimal giaBan;
    private Integer soTonKho;
    private String maVach;
    private String trangThai;
    private String nguoiTao;
    private LocalDateTime ngayTao;
    private String nguoiSuaCuoi;
    private LocalDateTime ngaySuaCuoi;
}
