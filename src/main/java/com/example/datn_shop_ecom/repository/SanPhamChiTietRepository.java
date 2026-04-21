package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.SanPhamChiTiet;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Long>, JpaSpecificationExecutor<SanPhamChiTiet> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SanPhamChiTiet s SET s.trangThai = :trangThai WHERE s.sanPham.id = :sanPhamId")
    void updateTrangThaiBySanPhamId(@Param("sanPhamId") Long sanPhamId, @Param("trangThai") String trangThai);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SanPhamChiTiet s SET s.trangThai = :trangThai WHERE s.id = :id")
    void updateTrangThaiById(@Param("id") Long id, @Param("trangThai") String trangThai);

    List<SanPhamChiTiet> findBySanPhamId(Long sanPhamId);

    // POS: tìm kiếm sản phẩm theo tên / mã / mã vạch, chỉ lấy hàng còn tồn
    @Query("SELECT s FROM SanPhamChiTiet s " +
           "WHERE s.trangThai = '1' AND s.soTonKho > 0 " +
           "AND (:kw IS NULL OR :kw = '' OR " +
           "  LOWER(s.sanPham.tenSanPham) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "  LOWER(s.maSanPhamChiTiet)   LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "  LOWER(s.maVach)             LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<SanPhamChiTiet> searchForPOS(@Param("kw") String keyword, Pageable pageable);

    // POS: tìm theo mã vạch (barcode)
    Optional<SanPhamChiTiet> findByMaVach(String maVach);

    // POS: trừ tồn kho an toàn (chỉ trừ khi tồn đủ) — trả về số rows affected
    @Modifying
    @Transactional
    @Query("UPDATE SanPhamChiTiet s SET s.soTonKho = s.soTonKho - :sl WHERE s.id = :id AND s.soTonKho >= :sl")
    int reduceStock(@Param("id") Long id, @Param("sl") int soLuong);

    // POS: hoàn trả tồn kho khi xóa/giảm SL
    @Modifying
    @Transactional
    @Query("UPDATE SanPhamChiTiet s SET s.soTonKho = s.soTonKho + :sl WHERE s.id = :id")
    void restoreStock(@Param("id") Long id, @Param("sl") int soLuong);

    @Query("SELECT MAX(s.giaBan) FROM SanPhamChiTiet s")
    BigDecimal findMaxPrice();
}

