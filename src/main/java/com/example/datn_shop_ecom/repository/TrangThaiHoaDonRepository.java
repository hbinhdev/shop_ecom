package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrangThaiHoaDonRepository extends JpaRepository<TrangThaiHoaDon, Long> {
    Optional<TrangThaiHoaDon> findByTenTrangThai(String tenTrangThai);
}

