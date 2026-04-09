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

    @Modifying
    @Query("UPDATE SanPham s SET s.xoaMem = :xoaMem WHERE s.id = :id")
    void updateXoaMem(@Param("id") Long id, @Param("xoaMem") Boolean xoaMem);

    java.util.List<SanPham> findAllByXoaMemFalse();
}

