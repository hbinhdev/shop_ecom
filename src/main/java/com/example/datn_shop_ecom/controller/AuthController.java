package com.example.datn_shop_ecom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/403")
    public String accessDenied() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("--- [DEBUG 403] ---");
            System.out.println("Email: " + auth.getName());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Is Authenticated: " + auth.isAuthenticated());
        } else {
            System.out.println("--- [DEBUG 403] No authentication found! ---");
        }
        return "admin/403";
    }

    @GetMapping("/error")
    public String error() {
        return "admin/error";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}