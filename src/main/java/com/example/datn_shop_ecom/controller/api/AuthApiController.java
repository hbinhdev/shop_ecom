package com.example.datn_shop_ecom.controller.api;

import com.example.datn_shop_ecom.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.get("email"),
                            loginRequest.get("password")
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "accessToken", jwt,
                "tokenType", "Bearer"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email hoặc mật khẩu không chính xác!"
            ));
        }
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> loginRequest, jakarta.servlet.http.HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.get("email"),
                            loginRequest.get("matKhau")
                    )
            );

            String jwt = tokenProvider.generateToken(authentication);
            
            // Thiết lập Cookie cho Admin
            jakarta.servlet.http.Cookie adminCookie = new jakarta.servlet.http.Cookie("ADMIN_AUTH", jwt);
            adminCookie.setHttpOnly(true);
            adminCookie.setSecure(false); // Đặt true nếu dùng HTTPS
            adminCookie.setPath("/"); // Để Filter có thể đọc được ở mọi path
            adminCookie.setMaxAge(86400); // 1 ngày
            response.addCookie(adminCookie);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", jwt,
                "username", authentication.getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email hoặc mật khẩu admin không chính xác!"
            ));
        }
    }
}

