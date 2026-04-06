-- Script sửa lỗi hiển thị tiếng Việt (Unicode) cho dự án Peak Sneaker
-- Bạn hãy copy toàn bộ nội dung này và chạy trong SQL Server Management Studio (SSMS)

-- 1. Các bảng thuộc tính sản phẩm mới (Attribute Management)
ALTER TABLE danh_muc ALTER COLUMN ten_danh_muc NVARCHAR(255);
ALTER TABLE thuong_hieu ALTER COLUMN ten_thuong_hieu NVARCHAR(255);
ALTER TABLE kieu_dang ALTER COLUMN ten_kieu_dang NVARCHAR(255);
ALTER TABLE chat_lieu ALTER COLUMN ten_chat_lieu NVARCHAR(255);
ALTER TABLE xuat_xu ALTER COLUMN ten_xuat_xu NVARCHAR(255);
ALTER TABLE de_giay ALTER COLUMN ten_de_giay NVARCHAR(255);
ALTER TABLE nha_san_xuat ALTER COLUMN ten_nha_san_xuat NVARCHAR(255);

-- 2. Các bảng thuộc tính cũ
ALTER TABLE mau_sac ALTER COLUMN ten_mau_sac NVARCHAR(255);
ALTER TABLE kich_thuoc ALTER COLUMN ten_kich_thuoc NVARCHAR(255);

-- 3. Sản phẩm và Biến thể
ALTER TABLE san_pham ALTER COLUMN ten_san_pham NVARCHAR(255);
ALTER TABLE san_pham ALTER COLUMN mo_ta NVARCHAR(MAX);
ALTER TABLE san_pham_chi_tiet ALTER COLUMN trang_thai NVARCHAR(50);

-- 4. Khách hàng, Nhân viên, Vai trò
ALTER TABLE khach_hang ALTER COLUMN ten_day_du NVARCHAR(255);
ALTER TABLE khach_hang ALTER COLUMN gioi_tinh NVARCHAR(20);

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('nhan_vien') AND name = 'ten_day_du')
    ALTER TABLE nhan_vien ALTER COLUMN ten_day_du NVARCHAR(255);

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('vai_tro') AND name = 'ten')
    ALTER TABLE vai_tro ALTER COLUMN ten NVARCHAR(255);

-- 5. Hóa đơn và các bảng liên quan đến nội dung tiếng Việt
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'trang_thai_hoa_don')
    ALTER TABLE hoa_don ALTER COLUMN trang_thai_hoa_don NVARCHAR(50);

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('hoa_don') AND name = 'mo_ta')
    ALTER TABLE hoa_don ALTER COLUMN mo_ta NVARCHAR(MAX);

PRINT 'DA CAP NHAT TAT CA CAC COT THANH NVARCHAR DE HO TRO TIENG VIET';
GO