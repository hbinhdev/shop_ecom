USE peak_sneaker;
GO

-- =========================================================================
-- 1. DROP FOREIGN KEYS
-- =========================================================================
DECLARE @DropConstraints NVARCHAR(max) = ''

-- Tìm khóa ngoại trong san_pham
SELECT @DropConstraints += 'ALTER TABLE [' + t.name + '] DROP CONSTRAINT [' + fk.name + ']; '
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
INNER JOIN sys.tables t ON fkc.parent_object_id = t.object_id
INNER JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id
WHERE t.name = 'san_pham' 
  AND c.name IN ('id_thuong_hieu', 'id_xuat_xu', 'id_co_giay', 'id_chat_lieu', 'id_vi_tri', 'id_phong_cach');

-- Tìm khóa ngoại trong san_pham_chi_tiet
SELECT @DropConstraints += 'ALTER TABLE [' + t.name + '] DROP CONSTRAINT [' + fk.name + ']; '
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
INNER JOIN sys.tables t ON fkc.parent_object_id = t.object_id
INNER JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id
WHERE t.name = 'san_pham_chi_tiet' 
  AND c.name IN ('id_loai_san', 'id_form_chan');

-- Thực thi lệnh DROP constraint
EXEC sp_executesql @DropConstraints;
GO

-- =========================================================================
-- 2. DROP CỘT KHÓA NGOẠI
-- =========================================================================
-- Xóa các cột trên bảng `san_pham`
IF COL_LENGTH('san_pham', 'id_thuong_hieu') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_thuong_hieu;
IF COL_LENGTH('san_pham', 'id_xuat_xu') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_xuat_xu;
IF COL_LENGTH('san_pham', 'id_co_giay') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_co_giay;
IF COL_LENGTH('san_pham', 'id_chat_lieu') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_chat_lieu;
IF COL_LENGTH('san_pham', 'id_vi_tri') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_vi_tri;
IF COL_LENGTH('san_pham', 'id_phong_cach') IS NOT NULL ALTER TABLE san_pham DROP COLUMN id_phong_cach;
GO

-- Xóa các cột trên bảng `san_pham_chi_tiet`
IF COL_LENGTH('san_pham_chi_tiet', 'id_loai_san') IS NOT NULL ALTER TABLE san_pham_chi_tiet DROP COLUMN id_loai_san;
IF COL_LENGTH('san_pham_chi_tiet', 'id_form_chan') IS NOT NULL ALTER TABLE san_pham_chi_tiet DROP COLUMN id_form_chan;
GO

-- =========================================================================
-- 3. DROP BẢNG ENTITY
-- =========================================================================
IF OBJECT_ID('thuong_hieu', 'U') IS NOT NULL DROP TABLE thuong_hieu;
IF OBJECT_ID('xuat_xu', 'U') IS NOT NULL DROP TABLE xuat_xu;
IF OBJECT_ID('co_giay', 'U') IS NOT NULL DROP TABLE co_giay;
IF OBJECT_ID('chat_lieu', 'U') IS NOT NULL DROP TABLE chat_lieu;
IF OBJECT_ID('vi_tri', 'U') IS NOT NULL DROP TABLE vi_tri;
IF OBJECT_ID('phong_cach_choi', 'U') IS NOT NULL DROP TABLE phong_cach_choi;
IF OBJECT_ID('loai_san', 'U') IS NOT NULL DROP TABLE loai_san;
IF OBJECT_ID('form_chan', 'U') IS NOT NULL DROP TABLE form_chan;
GO

PRINT 'Thành công xóa khóa ngoại, cột, và 8 bảng Entity!';
