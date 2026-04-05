package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.PhongCachChoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongCachChoiRepository extends JpaRepository<PhongCachChoi, Long> {
}
