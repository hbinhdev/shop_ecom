package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.ThuongHieu;
import com.example.datn_shop_ecom.repository.ThuongHieuRepository;
import com.example.datn_shop_ecom.service.ThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ThuongHieuServiceImpl implements ThuongHieuService {
    @Autowired
    private ThuongHieuRepository repo;

    @Override
    public List<ThuongHieu> findAll() {
        return repo.findAll();
    }
}

