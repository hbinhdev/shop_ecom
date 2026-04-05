package com.example.datn_shop_ecom.config;

import com.example.datn_shop_ecom.entity.*;
import com.example.datn_shop_ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ThuongHieuRepository thuongHieuRepo;
    private final XuatXuRepository xuatXuRepo;
    private final CoGiayRepository coGiayRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final ViTriRepository viTriRepo;
    private final PhongCachChoiRepository phongCachChoiRepo;
    private final LoaiSanRepository loaiSanRepo;
    private final FormChanRepository formChanRepo;
    private final MauSacRepository mauSacRepo;
    private final KichThuocRepository kichThuocRepo;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedThuongHieu();
        seedXuatXu();
        seedCoGiay();
        seedChatLieu();
        seedViTri();
        seedPhongCachChoi();
        seedLoaiSan();
        seedFormChan();
        seedMauSac();
        seedKichThuoc();
        System.out.println("====== ĐÃ CHẠY DATABASE SEEDER (BƠM DỮ LIỆU THIẾU) ======");
    }

    private void seedThuongHieu() {
        if (thuongHieuRepo.count() == 0) {
            List.of("Nike", "Adidas", "Puma", "Kamito", "Mizuno").forEach(name -> {
                thuongHieuRepo.save(ThuongHieu.builder().tenThuongHieu(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedXuatXu() {
        if (xuatXuRepo.count() == 0) {
            List.of("Vi\u1EC7t Nam", "Th\u00E1i Lan", "M\u1EF9", "Trung Qu\u1ED1c").forEach(name -> {
                xuatXuRepo.save(XuatXu.builder().tenXuatXu(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedCoGiay() {
        if (coGiayRepo.count() == 0) {
            List.of("C\u1ED5 th\u1EA5p", "C\u1ED5 l\u1EEDng", "C\u1ED5 cao").forEach(name -> {
                coGiayRepo.save(CoGiay.builder().tenCoGiay(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedChatLieu() {
        if (chatLieuRepo.count() == 0) {
            List.of("Da t\u1ED5ng h\u1EE3p", "Da th\u1EADt", "V\u1EA3i d\u1EC7t Flyknit", "V\u1EA3i l\u01B0\u1EDBi mesh", "Nh\u1EF1a t\u1ED5ng h\u1EE3p").forEach(name -> {
                chatLieuRepo.save(ChatLieu.builder().tenChatLieu(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedViTri() {
        if (viTriRepo.count() == 0) {
            List.of("Ti\u1EC1n \u0111\u1EA1o", "Ti\u1EC1n v\u1EC7", "H\u1EADu v\u1EC7", "Th\u1EE7 m\u00F4n").forEach(name -> {
                viTriRepo.save(ViTri.builder().tenViTri(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedPhongCachChoi() {
        if (phongCachChoiRepo.count() == 0) {
            List.of("T\u1ED1c \u0111\u1ED9", "K\u1EF9 thu\u1EADt", "Ki\u1EC3m so\u00E1t", "S\u1EE9c m\u1EA1nh").forEach(name -> {
                phongCachChoiRepo.save(PhongCachChoi.builder().tenPhongCach(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedLoaiSan() {
        if (loaiSanRepo.count() == 0) {
            List.of("S\u00E2n c\u1ECF t\u1EF1 nhi\u00EAn (FG)", "S\u00E2n c\u1ECF nh\u00E2n t\u1EA1o (TF)", "S\u00E2n trong nh\u00E0 (IC)").forEach(name -> {
                loaiSanRepo.save(LoaiSan.builder().tenLoaiSan(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedFormChan() {
        if (formChanRepo.count() == 0) {
            List.of("Form \u00F4m", "Form b\u00E8", "Form chu\u1EA9n").forEach(name -> {
                formChanRepo.save(FormChan.builder().tenForm(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }

    private void seedMauSac() {
        if (mauSacRepo.count() == 0) {
            List<String> mauSacs = List.of("Xanh dương", "Trắng", "Đen", "Đỏ", "Vàng", "Cam", "Xanh lá", "Tím");
            for (int i = 0; i < mauSacs.size(); i++) {
                String ma = String.format("MS%05d", i + 1);
                mauSacRepo.save(MauSac.builder()
                        .maMau(ma)
                        .tenMauSac(mauSacs.get(i))
                        .nguoiTao("System")
                        .ngayTao(LocalDateTime.now())
                        .xoaMem(false).build());
            }
        }
    }

    private void seedKichThuoc() {
        if (kichThuocRepo.count() == 0) {
            List.of("39", "40", "41", "42", "43").forEach(name -> {
                kichThuocRepo.save(KichThuoc.builder().tenKichThuoc(name).nguoiTao("System").ngayTao(LocalDateTime.now()).xoaMem(false).build());
            });
        }
    }
}
