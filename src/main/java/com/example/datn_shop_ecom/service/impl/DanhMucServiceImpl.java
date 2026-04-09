package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.DanhMuc;
import com.example.datn_shop_ecom.repository.DanhMucRepository;
import com.example.datn_shop_ecom.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DanhMucServiceImpl implements DanhMucService {
    @Autowired
    private DanhMucRepository repo;

    @Override
    public List<DanhMuc> findAll() {
        return repo.findAll();
    }
}

