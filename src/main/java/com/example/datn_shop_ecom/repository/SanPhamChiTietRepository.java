package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long>, JpaSpecificationExecutor<SanPhamChiTiet> {
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SanPhamChiTiet s SET s.trangThai = :trangThai WHERE s.sanPham.id = :sanPhamId")
    void updateTrangThaiBySanPhamId(@Param("sanPhamId") Long sanPhamId, @Param("trangThai") String trangThai);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SanPhamChiTiet s SET s.trangThai = :trangThai WHERE s.id = :id")
    void updateTrangThaiById(@Param("id") Long id, @Param("trangThai") String trangThai);

    List<SanPhamChiTiet> findBySanPhamId(Long sanPhamId);
}

