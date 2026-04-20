package com.example.datn_shop_ecom.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorMessage;

        if (exception instanceof BadCredentialsException) {
            errorMessage = "Email hoặc mật khẩu không đúng";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Tài khoản của bạn đã bị vô hiệu hóa";
        } else if (exception instanceof LockedException) {
            errorMessage = "Tài khoản của bạn đã bị khóa";
        } else {
            errorMessage = "Đăng nhập thất bại. Vui lòng thử lại";
        }

        System.out.println("--- [FAILURE] Login failed: " + exception.getMessage());

        String encoded = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        // Kiểm tra xem là đăng nhập từ Admin hay Client để quay về đúng trang
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/admin/login")) {
            response.sendRedirect("/admin/login?error=" + encoded);
        } else {
            response.sendRedirect("/dang-nhap?error=" + encoded);
        }
    }
}