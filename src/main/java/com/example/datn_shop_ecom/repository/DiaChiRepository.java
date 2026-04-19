package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi, Long> {
    List<DiaChi> findByKhachHangId(Long khachHangId);

    Optional<DiaChi> findByKhachHangIdAndTenNguoiNhanAndSoDienThoaiNguoiNhanAndTinhThanhPhoAndQuanHuyenAndXaPhuongAndChiTiet(
            Long khachHangId, String ten, String sdt, String ttp, String qh, String xp, String ct);
}
