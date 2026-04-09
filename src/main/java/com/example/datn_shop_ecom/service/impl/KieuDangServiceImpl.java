package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.KieuDang;
import com.example.datn_shop_ecom.repository.KieuDangRepository;
import com.example.datn_shop_ecom.service.KieuDangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KieuDangServiceImpl implements KieuDangService {
    @Autowired
    private KieuDangRepository repo;

    @Override
    public List<KieuDang> findAll() {
        return repo.findAll();
    }
}

