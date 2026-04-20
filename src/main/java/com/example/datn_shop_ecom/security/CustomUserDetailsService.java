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
            System.out.println("DEBUG: Portal ADMIN detected - Searching NHAN_VIEN only");
            return loadNhanVienOnly(email); // Chỉ tìm trong NhanVien, không nhảy sang KhachHang
        } else {
            System.out.println("DEBUG: Portal CLIENT detected - Searching KHACH_HANG only");
            return loadKhachHangOnly(email); // Chỉ tìm trong KhachHang, không nhảy sang NhanVien
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

    public UserDetails loadKhachHangOnly(String email) {
        KhachHang kh = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản khách hàng: " + email));
        String role = (kh.getVaiTro() != null) ? kh.getVaiTro().getMa() : "ROLE_CUSTOMER";
        boolean active = !Boolean.TRUE.equals(kh.getXoaMem());
        return User.builder()
                .username(kh.getEmail())
                .password(kh.getMatKhau() != null ? kh.getMatKhau().trim() : null)
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!active)
                .accountLocked(!active)
                .build();
    }

    public UserDetails loadNhanVienOnly(String email) {
        System.out.println("DEBUG: Executing loadNhanVienOnly for email: " + email);
        NhanVien nv = nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Could not find NhanVien with email: " + email);
                    return new UsernameNotFoundException("Không tìm thấy tài khoản nhân viên: " + email);
                });
        
        // Lấy mã vai trò và làm sạch (Trim + Uppercase)
        String role = (nv.getVaiTro() != null && nv.getVaiTro().getMa() != null) 
                      ? nv.getVaiTro().getMa().trim().toUpperCase() 
                      : "ROLE_EMPLOYEE";

        // Nếu mã không bắt đầu bằng ROLE_, tự thêm vào cho chuẩn Spring Security
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        System.out.println("DEBUG: Loaded NhanVien: " + nv.getTenDayDu() + " | Role: " + role);

        // Ép buộc: Nhân viên thì vai trò PHẢI là ADMIN hoặc EMPLOYEE
        if (role.equals("ROLE_USER") || role.equals("ROLE_CUSTOMER") || role.equals("ROLE_GUEST")) {
            System.out.println("WARN: Phat hien Nhan vien mang quyen Khach hang. Dang ep ve ROLE_EMPLOYEE for: " + email);
            role = "ROLE_EMPLOYEE"; 
        }

        boolean active = !Boolean.TRUE.equals(nv.getXoaMem());
        return User.builder()
                .username(nv.getEmail())
                .password(nv.getMatKhau() != null ? nv.getMatKhau().trim() : null)
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!active)
                .accountLocked(!active)
                .build();
    }
}
