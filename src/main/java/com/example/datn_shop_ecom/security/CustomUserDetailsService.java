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
        // Lấy thông tin request hiện tại
        org.springframework.web.context.request.RequestAttributes attrs = 
            org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        
        String uri = "UNKNOWN";
        String portal = null;
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes) {
            jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) attrs).getRequest();
            uri = request.getRequestURI();
            portal = (String) request.getSession().getAttribute("login_portal");
        }
        
        System.out.println("--- [AUTH TRACE] Email: " + email + " | URI: " + uri + " | Portal: " + portal);
        
        // 0. Kiểm tra nếu là các link đăng nhập đặc thù của Client
        boolean isClientLogin = uri.contains("/api/client/login") || uri.contains("/dang-nhap");

        // 1. Nếu đã có nhãn Portal trong Session và KHÔNG phải đang đăng nhập Client mới
        if ("ADMIN".equals(portal) && !isClientLogin) {
            return loadNhanVienOnly(email);
        } else if ("CLIENT".equals(portal)) {
            return loadKhachHangOnly(email);
        }

        // 2. Nếu chưa có nhãn hoặc đang cố đăng nhập Client, dùng URI để đoán
        boolean isAdminPath = uri.contains("/admin") || uri.contains("/perform_login_admin");
        if (isAdminPath) {
            return loadNhanVienFirst(email);
        } else {
            return loadKhachHangFirst(email);
        }
    }

    /**
     * Nạp người dùng dựa trên Email và danh sách Quyền từ Token (JWT)
     */
    public UserDetails loadUserByUsernameAndRoles(String email, List<String> roles) {
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_EMPLOYEE")) {
            return loadNhanVienOnly(email);
        } else {
            return loadKhachHangOnly(email);
        }
    }

    private UserDetails loadNhanVienFirst(String email) {
        NhanVien nhanVien = nhanVienRepository.findByEmail(email).orElse(null);
        if (nhanVien != null) {
            String role = (nhanVien.getVaiTro() != null) ? nhanVien.getVaiTro().getMa() : "ROLE_EMPLOYEE";
            System.out.println("DEBUG: Dang nhap Nhan vien: " + email + " - Quyen: " + role);
            
            boolean active = !Boolean.TRUE.equals(nhanVien.getXoaMem());
            return User.builder()
                    .username(nhanVien.getEmail())
                    .password(nhanVien.getMatKhau())
                    .authorities(List.of(new SimpleGrantedAuthority(role)))
                    .disabled(!active)
                    .accountLocked(!active)
                    .build();
        }
        // Nếu không thấy trong Nhân viên, thử tìm trong Khách hàng
        return loadKhachHangOnly(email);
    }

    private UserDetails loadKhachHangFirst(String email) {
        KhachHang khachHang = khachHangRepository.findByEmail(email).orElse(null);
        if (khachHang != null) {
            if (Boolean.TRUE.equals(khachHang.getXoaMem())) {
                throw new UsernameNotFoundException("Tài khoản khách hàng đã bị xóa: " + email);
            }
            String role = (khachHang.getVaiTro() != null) ? khachHang.getVaiTro().getMa() : "ROLE_CUSTOMER";
            boolean active = !Boolean.TRUE.equals(khachHang.getXoaMem());
            return User.builder()
                    .username(khachHang.getEmail())
                    .password(khachHang.getMatKhau())
                    .authorities(List.of(new SimpleGrantedAuthority(role)))
                    .disabled(!active)
                    .accountLocked(!active)
                    .build();
        }
        // Nếu không thấy trong Khách hàng, thử tìm trong Nhân viên
        return loadNhanVienOnly(email);
    }

    private UserDetails loadKhachHangOnly(String email) {
        KhachHang kh = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
        String role = (kh.getVaiTro() != null) ? kh.getVaiTro().getMa() : "ROLE_CUSTOMER";
        boolean active = !Boolean.TRUE.equals(kh.getXoaMem());
        return User.builder()
                .username(kh.getEmail())
                .password(kh.getMatKhau())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!active)
                .accountLocked(!active)
                .build();
    }

    private UserDetails loadNhanVienOnly(String email) {
        NhanVien nv = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
        String role = (nv.getVaiTro() != null) ? nv.getVaiTro().getMa() : "ROLE_EMPLOYEE";
        boolean active = !Boolean.TRUE.equals(nv.getXoaMem());
        return User.builder()
                .username(nv.getEmail())
                .password(nv.getMatKhau())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!active)
                .accountLocked(!active)
                .build();
    }
}
