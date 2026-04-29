package com.example.datn_shop_ecom.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- PROVIDER CHO ADMIN ---
    @Bean
    public DaoAuthenticationProvider adminProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Ép buộc chỉ tìm trong bảng NhanVien
        provider.setUserDetailsService(email -> userDetailsService.loadNhanVienOnly(email));
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    // --- PROVIDER CHO CLIENT ---
    @Bean
    public DaoAuthenticationProvider clientProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Ép buộc chỉ tìm trong bảng KhachHang
        provider.setUserDetailsService(email -> userDetailsService.loadKhachHangOnly(email));
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean(name = "clientAuthenticationManager")
    public AuthenticationManager clientAuthenticationManager() {
        return new org.springframework.security.authentication.ProviderManager(clientProvider());
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // 1. Cấu hình bảo mật cho trang ADMIN (Chạy chế độ STATELESS dùng JWT)
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**", "/api/admin/**")
            .authenticationProvider(adminProvider()) 
            .csrf(csrf -> csrf.disable()) // Stateless không cần CSRF truyền thống
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/api/login").permitAll()
                
                // Phân quyền chi tiết cho Sản phẩm (sp)
                // Nhân viên & Admin đều được xem danh sách, biến thể và chi tiết
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/admin/san-pham", "/admin/san-pham/bien-the", "/admin/san-pham/detail/**").hasAnyRole("ADMIN", "EMPLOYEE")
                
                // Chỉ Admin mới được Thêm mới, Sửa, Lưu hoặc Đổi trạng thái Sản phẩm
                .requestMatchers("/admin/san-pham/create", "/admin/san-pham/edit/**", "/admin/san-pham/api/save", "/admin/san-pham/toggle-status/**").hasRole("ADMIN")

                // Phân quyền chi tiết cho Biến thể sản phẩm (Variant)
                // Chỉ Admin mới được Thêm/Sửa/Xóa/Đổi trạng thái biến thể
                .requestMatchers("/api/admin/product-variant/create-all-with-images", "/api/admin/product-variant/save-simple", 
                               "/api/admin/product-variant/variant/save", "/api/admin/product-variant/variant/toggle-status/**", 
                               "/api/admin/product-variant/variant/delete/**").hasRole("ADMIN")
                
                // Phân quyền chi tiết cho Phiếu giảm giá (pgg)
                // Nhân viên & Admin đều được xem danh sách và xem form (nhưng nhân viên sẽ bị ẩn nút lưu ở giao diện)
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/admin/phieu-giam-gia", "/admin/phieu-giam-gia/edit/**").hasAnyRole("ADMIN", "EMPLOYEE")
                // Chỉ Admin mới được Tạo mới, Lưu hoặc Xóa
                .requestMatchers("/admin/phieu-giam-gia/create", "/admin/phieu-giam-gia/save", "/admin/phieu-giam-gia/toggle-status/**", "/admin/phieu-giam-gia/soft-delete/**").hasRole("ADMIN")

                .requestMatchers("/admin/**", "/api/admin/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/403");
                })
            );

        return http.build();
    }

    // 2. Cấu hình bảo mật cho trang CLIENT
    @Bean
    @Order(2)
    public SecurityFilterChain clientFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authenticationProvider(clientProvider()) // Dùng bộ xác thực Client
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                corsConfiguration.setAllowedOrigins(List.of("*"));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                return corsConfiguration;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/uploads/**", "/assets/**", "/vendor/**",
                    "/", "/index", "/san-pham/**", "/error", "/403", "/404",
                    "/dang-nhap", "/dang-ky", "/ve-chung-toi", "/phieu-giam-gia", "/tra-cuu", 
                    "/gio-hang", "/api/client/**", "/api/auth/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/dang-nhap")
                .loginProcessingUrl("/perform_login_client")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/dang-nhap?logout")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/dang-nhap?sessionExpired=true")
            );

        return http.build();
    }
}