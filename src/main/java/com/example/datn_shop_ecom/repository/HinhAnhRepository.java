package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Long> {
    List<HinhAnh> findBySanPhamId(Long sanPhamId);
    List<HinhAnh> findBySanPhamChiTietId(Long sanPhamChiTietId);
    List<HinhAnh> findBySanPhamChiTietIdIn(List<Long> ids);

    @org.springframework.transaction.annotation.Transactional
    void deleteBySanPhamChiTietId(Long sanPhamChiTietId);
}
