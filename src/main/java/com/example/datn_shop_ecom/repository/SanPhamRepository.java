package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    @Query("SELECT s FROM SanPham s WHERE " +
           "(:search IS NULL OR s.tenSanPham LIKE %:search% OR s.maSanPham LIKE %:search%) AND " +
           "(:trangThai IS NULL OR s.xoaMem = :trangThai) AND " +
           "(:idDanhMuc IS NULL OR s.danhMuc.id = :idDanhMuc) AND " +
           "(:idThuongHieu IS NULL OR s.thuongHieu.id = :idThuongHieu) AND " +
           "(:idKieuDang IS NULL OR s.kieuDang.id = :idKieuDang) AND " +
           "(:idChatLieu IS NULL OR s.chatLieu.id = :idChatLieu)")
    Page<SanPham> findByFilters(
            @Param("search") String search,
            @Param("trangThai") Boolean trangThai,
            @Param("idDanhMuc") Long idDanhMuc,
            @Param("idThuongHieu") Long idThuongHieu,
            @Param("idKieuDang") Long idKieuDang,
            @Param("idChatLieu") Long idChatLieu,
            Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanPham s " +
           "LEFT JOIN s.danhSachChiTiet spct " +
           "WHERE (s.xoaMem = false) " +
           "AND (:search IS NULL OR s.tenSanPham LIKE %:search% OR s.maSanPham LIKE %:search%) " +
           "AND (:idDanhMuc IS NULL OR s.danhMuc.id = :idDanhMuc) " +
           "AND (:idThuongHieu IS NULL OR s.thuongHieu.id = :idThuongHieu) " +
           "AND (:idKieuDang IS NULL OR s.kieuDang.id = :idKieuDang) " +
           "AND (:idChatLieu IS NULL OR s.chatLieu.id = :idChatLieu) " +
           "AND (:idMauSac IS NULL OR spct.mauSac.id = :idMauSac) " +
           "AND (:idKichThuoc IS NULL OR spct.kichThuoc.id = :idKichThuoc)")
    java.util.List<SanPham> findByClientFilters(
            @Param("search") String search,
            @Param("idDanhMuc") Long idDanhMuc,
            @Param("idThuongHieu") Long idThuongHieu,
            @Param("idKieuDang") Long idKieuDang,
            @Param("idChatLieu") Long idChatLieu,
            @Param("idMauSac") Long idMauSac,
            @Param("idKichThuoc") Long idKichThuoc,
            org.springframework.data.domain.Sort sort);

    @Modifying
    @Query("UPDATE SanPham s SET s.xoaMem = :xoaMem WHERE s.id = :id")
    void updateXoaMem(@Param("id") Long id, @Param("xoaMem") Boolean xoaMem);

    java.util.List<SanPham> findAllByXoaMemFalse();

    @Query("SELECT s FROM SanPham s " +
           "LEFT JOIN s.danhSachChiTiet spct " +
           "LEFT JOIN ChiTietHoaDon cthd ON cthd.sanPhamChiTiet.id = spct.id " +
           "WHERE s.xoaMem = false " +
           "GROUP BY s " +
           "ORDER BY SUM(COALESCE(cthd.soLuong, 0)) DESC")
    java.util.List<SanPham> findTopBestSellers(Pageable pageable);

    java.util.List<SanPham> findAllByXoaMemFalseOrderByIdDesc(Pageable pageable);
}

