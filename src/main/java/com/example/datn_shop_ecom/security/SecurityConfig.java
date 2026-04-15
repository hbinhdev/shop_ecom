package com.example.datn_shop_ecom.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    // ----------------------------------------------------------------
    // 1. Password Encoder - BCrypt
    // ----------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ----------------------------------------------------------------
    // 2. Authentication Provider - kết nối UserDetailsService + BCrypt
    // ----------------------------------------------------------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ----------------------------------------------------------------
    // 3. Authentication Manager
    // ----------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ----------------------------------------------------------------
    // 4. Session Event Publisher - theo dõi session hết hạn
    // ----------------------------------------------------------------
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // ----------------------------------------------------------------
    // 5. Security Filter Chain - quy tắc phân quyền URL
    // ----------------------------------------------------------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                corsConfiguration.setAllowedOrigins(java.util.List.of("*"));
                corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
                return corsConfiguration;
            }))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh & trang công khai
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/uploads/**", "/assets/**", "/vendor/**",
                    "/client/**",
                    "/", "/index", "/san-pham/**", "/admin/login", "/error", "/403", "/404",
                    "/dang-nhap", "/dang-ky", "/ve-chung-toi", "/phieu-giam-gia", "/tra-cuu", 
                    "/gio-hang", "/thanh-toan", "/tai-khoan",
                    "/api/client/**", "/api/auth/**"
                ).permitAll()

                // Chỉ Admin: quản lý nhân viên, thống kê
                .requestMatchers("/admin/nhan-vien/**", "/admin/thong-ke/**").hasRole("ADMIN")

                // Admin hoặc Nhân viên: toàn bộ admin còn lại
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")

                // Đã đăng nhập (bất kỳ role)
                .anyRequest().authenticated()
            )

            // Cấu hình trang Login
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("email")
                .passwordParameter("matKhau")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )

            // Cấu hình Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Trang 403 Access Denied
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )

            // Session Management - giới hạn 1 session / tài khoản
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/admin/login?sessionExpired=true")
            );

        return http.build();
    }
}