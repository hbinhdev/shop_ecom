-- Script sửa lỗi hiển thị tiếng Việt (Unicode) cho dự án Peak Sneaker
-- Bạn hãy copy toàn bộ nội dung này và chạy trong SQL Server Management Studio (SSMS)

-- 1. Bảng MauSac
ALTER TABLE mau_sac ALTER COLUMN ten_mau_sac NVARCHAR(255);

-- 2. Bảng KichThuoc
ALTER TABLE kich_thuoc ALTER COLUMN ten_kich_thuoc NVARCHAR(255);

-- 3. Bảng SanPham
ALTER TABLE san_pham ALTER COLUMN ten_san_pham NVARCHAR(255);
ALTER TABLE san_pham ALTER COLUMN mo_ta NVARCHAR(MAX);

-- 4. Bảng KhachHang
ALTER TABLE khach_hang ALTER COLUMN ten_day_du NVARCHAR(255);
ALTER TABLE khach_hang ALTER COLUMN gioi_tinh NVARCHAR(20);

-- 5. Bảng SanPhamChiTiet
ALTER TABLE san_pham_chi_tiet ALTER COLUMN trang_thai NVARCHAR(50);

-- 6. Các bảng khác nếu có dữ liệu tiếng Việt (Đề phòng)
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('vai_tro') AND name = 'ten_vai_tro')
    ALTER TABLE vai_tro ALTER COLUMN ten_vai_tro NVARCHAR(255);

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'ten_day_du')
    ALTER TABLE nhan_vien ALTER COLUMN ten_day_du NVARCHAR(255);

PRINT 'Đã hoàn thành chuyển đổi sang NVARCHAR. Vui lòng kiểm tra lại dữ liệu.';
