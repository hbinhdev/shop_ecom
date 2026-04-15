package com.example.datn_shop_ecom.security;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Ưu tiên tìm trong bảng nhân viên trước
        NhanVien nhanVien = nhanVienRepository.findByEmail(email).orElse(null);
        if (nhanVien != null) {
            String role = (nhanVien.getVaiTro() != null) ? nhanVien.getVaiTro().getMa() : "ROLE_EMPLOYEE";
            // xoaMem=true → tài khoản bị vô hiệu hóa (toggle status trong UI)
            boolean active = !Boolean.TRUE.equals(nhanVien.getXoaMem());
            return User.builder()
                    .username(nhanVien.getEmail())
                    .password(nhanVien.getMatKhau())
                    .authorities(List.of(new SimpleGrantedAuthority(role)))
                    .disabled(!active)
                    .accountLocked(!active)
                    .build();
        }

        // Tìm trong bảng khách hàng
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));

        if (Boolean.TRUE.equals(khachHang.getXoaMem())) {
            throw new UsernameNotFoundException("Tài khoản đã bị xóa: " + email);
        }

        String role = (khachHang.getVaiTro() != null) ? khachHang.getVaiTro().getMa() : "ROLE_CUSTOMER";
        boolean enabled = "Hoạt động".equals(khachHang.getTrangThai());
        return User.builder()
                .username(khachHang.getEmail())
                .password(khachHang.getMatKhau())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!enabled)
                .accountLocked(!enabled)
                .build();
    }
}
