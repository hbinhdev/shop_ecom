package com.example.datn_shop_ecom.repository;

import com.example.datn_shop_ecom.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    @Query("SELECT n FROM NhanVien n " +
           "WHERE (:search IS NULL OR n.maNhanVien LIKE %:search% " +
           "OR n.tenDayDu LIKE %:search% " +
           "OR n.email LIKE %:search% OR n.soDienThoai LIKE %:search%) " +
           "AND (:idVaiTro IS NULL OR n.vaiTro.id = :idVaiTro) " +
           "AND (:xoaMem IS NULL OR n.xoaMem = :xoaMem)")
    org.springframework.data.domain.Page<NhanVien> findByFiltersPage(@Param("search") String search, 
                                                                    @Param("idVaiTro") Long idVaiTro, 
                                                                    @Param("xoaMem") Boolean xoaMem, 
                                                                    org.springframework.data.domain.Pageable pageable);

    @Query("SELECT n FROM NhanVien n " +
           "WHERE (:search IS NULL OR n.maNhanVien LIKE %:search% " +
           "OR n.tenDayDu LIKE %:search% " +
           "OR n.email LIKE %:search% OR n.soDienThoai LIKE %:search%) " +
           "AND (:idVaiTro IS NULL OR n.vaiTro.id = :idVaiTro) " +
           "AND (:xoaMem IS NULL OR n.xoaMem = :xoaMem) " +
           "ORDER BY n.ngayTao DESC")
    List<NhanVien> findByFilters(@Param("search") String search, 
                                @Param("idVaiTro") Long idVaiTro, 
                                @Param("xoaMem") Boolean xoaMem);

    List<NhanVien> findAllByXoaMemFalse();
    
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);
    Optional<NhanVien> findByEmail(String email);
    Optional<NhanVien> findBySoDienThoai(String soDienThoai);
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE NhanVien n SET n.xoaMem = :xoaMem, n.ngaySuaCuoi = CURRENT_TIMESTAMP, n.nguoiSuaCuoi = :nguoiSua WHERE n.id = :id")
    void updateStatus(@Param("id") Long id, @Param("xoaMem") Boolean xoaMem, @Param("nguoiSua") String nguoiSua);
}

