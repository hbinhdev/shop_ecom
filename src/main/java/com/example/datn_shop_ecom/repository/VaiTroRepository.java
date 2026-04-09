package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Long> {
    List<VaiTro> findAllByXoaMemFalse();
}

