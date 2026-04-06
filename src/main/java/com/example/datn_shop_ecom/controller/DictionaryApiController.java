package com.example.datn_shop_ecom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dictionary")
public class DictionaryApiController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/all")
    public ResponseEntity<?> getAllDictionaries() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("mauSac", entityManager.createQuery("SELECT m FROM MauSac m").getResultList());
        result.put("kichThuoc", entityManager.createQuery("SELECT k FROM KichThuoc k").getResultList());
        
        return ResponseEntity.ok(result);
    }
}
