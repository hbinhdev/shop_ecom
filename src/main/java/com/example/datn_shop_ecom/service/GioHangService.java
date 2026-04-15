package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.GioHang;
import com.example.datn_shop_ecom.entity.GioHangChiTiet;
import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import com.example.datn_shop_ecom.repository.GioHangChiTietRepository;
import com.example.datn_shop_ecom.repository.GioHangRepository;
import com.example.datn_shop_ecom.repository.KhachHangRepository;
import com.example.datn_shop_ecom.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GioHangService {

    @Autowired
    private GioHangRepository gioHangRepo;

    @Autowired
    private GioHangChiTietRepository chiTietRepo;

    @Autowired
    private KhachHangRepository khachHangRepo;

    @Autowired
    private SanPhamChiTietRepository spctRepo;

    public GioHang getOrCreateCart(Long khachHangId) {
        return gioHangRepo.findByKhachHangId(khachHangId)
                .orElseGet(() -> {
                    KhachHang kh = khachHangRepo.findById(khachHangId).orElse(null);
                    if (kh == null) return null;
                    GioHang gh = GioHang.builder()
                            .khachHang(kh)
                            .ngayTao(LocalDateTime.now())
                            .ngaySuaCuoi(LocalDateTime.now())
                            .build();
                    return gioHangRepo.save(gh);
                });
    }

    @Transactional
    public void addToCart(Long khachHangId, Long spctId, int qty) {
        GioHang gh = getOrCreateCart(khachHangId);
        if (gh == null) return;

        Optional<GioHangChiTiet> optCt = chiTietRepo.findByGioHangIdAndSanPhamChiTietId(gh.getId(), spctId);
        if (optCt.isPresent()) {
            GioHangChiTiet ct = optCt.get();
            ct.setSoLuong(ct.getSoLuong() + qty);
            chiTietRepo.save(ct);
        } else {
            SanPhamChiTiet spct = spctRepo.findById(spctId).orElse(null);
            if (spct != null) {
                GioHangChiTiet ct = GioHangChiTiet.builder()
                        .gioHang(gh)
                        .sanPhamChiTiet(spct)
                        .soLuong(qty)
                        .build();
                chiTietRepo.save(ct);
            }
        }
        gh.setNgaySuaCuoi(LocalDateTime.now());
        gioHangRepo.save(gh);
    }

    @Transactional
    public void syncCart(Long khachHangId, List<java.util.Map<String, Object>> items) {
        GioHang gh = getOrCreateCart(khachHangId);
        if (gh == null) return;

        for (java.util.Map<String, Object> item : items) {
            Long spctId = Long.valueOf(item.get("spctId").toString());
            int qty = Integer.parseInt(item.get("soLuong").toString());
            
            Optional<GioHangChiTiet> optCt = chiTietRepo.findByGioHangIdAndSanPhamChiTietId(gh.getId(), spctId);
            if (optCt.isPresent()) {
                // Sync rule: browser qty might be newer or we might want to sum?
                // Usually we replace or sum. Let's replace for sync.
                optCt.get().setSoLuong(qty);
                chiTietRepo.save(optCt.get());
            } else {
                SanPhamChiTiet spct = spctRepo.findById(spctId).orElse(null);
                if (spct != null) {
                    chiTietRepo.save(GioHangChiTiet.builder()
                            .gioHang(gh)
                            .sanPhamChiTiet(spct)
                            .soLuong(qty)
                            .build());
                }
            }
        }
    }
    
    public List<GioHangChiTiet> getCartItems(Long khachHangId) {
        GioHang gh = getOrCreateCart(khachHangId);
        if (gh == null) return java.util.Collections.emptyList();
        return chiTietRepo.findByGioHangId(gh.getId());
    }

    @Transactional
    public void removeItem(Long khachHangId, Long spctId) {
        GioHang gh = getOrCreateCart(khachHangId);
        if (gh == null) return;
        chiTietRepo.findByGioHangIdAndSanPhamChiTietId(gh.getId(), spctId)
                .ifPresent(ct -> chiTietRepo.delete(ct));
    }

    @Transactional
    public void clearCart(Long khachHangId) {
        GioHang gh = getOrCreateCart(khachHangId);
        if (gh != null) {
            List<GioHangChiTiet> items = chiTietRepo.findByGioHangId(gh.getId());
            chiTietRepo.deleteAll(items);
        }
    }
}
