package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.DeGiay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeGiayRepository extends JpaRepository<DeGiay, Long> {
}

