package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KichThuocRepository extends JpaRepository<KichThuoc, Long> {
    java.util.List<KichThuoc> findAllByXoaMemFalse();
}

