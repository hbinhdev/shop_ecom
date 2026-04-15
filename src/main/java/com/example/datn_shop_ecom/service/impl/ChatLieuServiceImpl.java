package com.example.datn_shop_ecom.service.impl;

import com.example.datn_shop_ecom.entity.ChatLieu;
import com.example.datn_shop_ecom.repository.ChatLieuRepository;
import com.example.datn_shop_ecom.service.ChatLieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatLieuServiceImpl implements ChatLieuService {
    @Autowired
    private ChatLieuRepository repo;

    @Override
    public List<ChatLieu> findAll() {
        return repo.findAll();
    }
}

