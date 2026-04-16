package com.example.datn_shop_ecom.config;

import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.entity.VaiTro;
import com.example.datn_shop_ecom.repository.NhanVienRepository;
import com.example.datn_shop_ecom.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Tạo vai trò nếu chưa có
        Map<String, String> roles = Map.of(
            "ROLE_ADMIN",    "Quản trị viên",
            "ROLE_EMPLOYEE", "Nhân viên",
            "ROLE_CUSTOMER", "Khách hàng"
        );

        roles.forEach((ma, ten) -> {
            if (vaiTroRepository.findByMa(ma).isEmpty()) {
                vaiTroRepository.save(VaiTro.builder()
                    .ma(ma)
                    .ten(ten)
                    .nguoiTao("system")
                    .ngayTao(LocalDateTime.now())
                    .xoaMem(false)
                    .build());
                System.out.println("[DataInitializer] Đã tạo vai trò: " + ma);
            }
        });

        // 2. Tạo tài khoản admin mặc định nếu chưa có
        if (nhanVienRepository.findByEmail("admin@peaksneaker.com").isEmpty()) {
            VaiTro admin = vaiTroRepository.findByMa("ROLE_ADMIN").orElseThrow();
            nhanVienRepository.save(NhanVien.builder()
                .maNhanVien("NV000001")
                .tenDayDu("Quản trị viên")
                .email("admin@peaksneaker.com")
                .matKhau(passwordEncoder.encode("Admin@123"))
                .trangThai("Hoạt động")
                .vaiTro(admin)
                .xoaMem(false)
                .nguoiTao("system")
                .ngayTao(LocalDateTime.now())
                .build());
            System.out.println("[DataInitializer] Đã tạo tài khoản admin mặc định");
            System.out.println("  Email   : admin@peaksneaker.com");
            System.out.println("  Password: Admin@123");
        }
    }
}