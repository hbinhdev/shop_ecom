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
        
        result.put("thuongHieu", entityManager.createQuery("SELECT t FROM ThuongHieu t").getResultList());
        result.put("xuatXu", entityManager.createQuery("SELECT x FROM XuatXu x").getResultList());
        result.put("coGiay", entityManager.createQuery("SELECT c FROM CoGiay c").getResultList());
        result.put("chatLieu", entityManager.createQuery("SELECT c FROM ChatLieu c").getResultList());
        result.put("viTri", entityManager.createQuery("SELECT v FROM ViTri v").getResultList());
        result.put("phongCachChoi", entityManager.createQuery("SELECT p FROM PhongCachChoi p").getResultList());
        result.put("loaiSan", entityManager.createQuery("SELECT l FROM LoaiSan l").getResultList());
        result.put("formChan", entityManager.createQuery("SELECT f FROM FormChan f").getResultList());
        result.put("mauSac", entityManager.createQuery("SELECT m FROM MauSac m").getResultList());
        result.put("kichThuoc", entityManager.createQuery("SELECT k FROM KichThuoc k").getResultList());
        
        return ResponseEntity.ok(result);
    }
}
