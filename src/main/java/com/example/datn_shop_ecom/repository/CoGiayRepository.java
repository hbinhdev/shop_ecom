package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.CoGiay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoGiayRepository extends JpaRepository<CoGiay, Long> {
}
