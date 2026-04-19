package com.example.datn_shop_ecom.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.example.datn_shop_ecom.config.AppProperties;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final AppProperties appProperties;

    public JwtTokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        // Lấy danh sách các quyền (roles) để lưu vào Token
        java.util.List<String> roles = authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getJwtExpirationMs());

        SecretKey key = Keys.hmacShaKeyFor(appProperties.getJwtSecret().getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles) // Lưu danh sách roles vào claim
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public java.util.List<String> getRolesFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(appProperties.getJwtSecret().getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (java.util.List<String>) claims.get("roles");
    }

    public String getUsernameFromJWT(String token) {
        SecretKey key = Keys.hmacShaKeyFor(appProperties.getJwtSecret().getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(appProperties.getJwtSecret().getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            
        } catch (ExpiredJwtException ex) {
            
        } catch (UnsupportedJwtException ex) {
            
        } catch (IllegalArgumentException ex) {
            
        }
        return false;
    }
}

