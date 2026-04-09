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
        result.put("danhMuc", entityManager.createQuery("SELECT d FROM DanhMuc d").getResultList());
        result.put("thuongHieu", entityManager.createQuery("SELECT t FROM ThuongHieu t").getResultList());
        result.put("kieuDang", entityManager.createQuery("SELECT k FROM KieuDang k").getResultList());
        result.put("chatLieu", entityManager.createQuery("SELECT c FROM ChatLieu c").getResultList());
        
        return ResponseEntity.ok(result);
    }
}

