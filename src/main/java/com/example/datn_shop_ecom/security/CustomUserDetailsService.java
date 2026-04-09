package com.example.datn_shop_ecom.security;

import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.entity.NhanVien;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<NhanVien> nhanVien = nhanVienRepository.findByEmail(email);
        if (nhanVien.isPresent()) {
            NhanVien nv = nhanVien.get();
            String role = nv.getVaiTro() != null ? nv.getVaiTro().getTen() : "ADMIN";
            return new User(nv.getEmail(), nv.getMatKhau(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
        }

        Optional<KhachHang> khachHang = khachHangRepository.findByEmail(email);
        if (khachHang.isPresent()) {
            KhachHang kh = khachHang.get();
            return new User(kh.getEmail(), kh.getMatKhau(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + email);
    }
}

