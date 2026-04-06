package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.KieuDang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KieuDangRepository extends JpaRepository<KieuDang, Long> {
}
