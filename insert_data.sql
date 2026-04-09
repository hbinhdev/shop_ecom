USE [peak_sneaker_2]
GO

-- 1. Xóa dữ liệu cũ (Theo thứ tự bảng con trước, bảng cha sau)
DELETE FROM [dbo].[lich_su_hoa_don];
DELETE FROM [dbo].[lich_su_thanh_toan];
DELETE FROM [dbo].[dia_chi];
DELETE FROM [dbo].[chi_tiet_hoa_don];
DELETE FROM [dbo].[hoa_don];
DELETE FROM [dbo].[hinh_anh];
DELETE FROM [dbo].[san_pham_chi_tiet];
DELETE FROM [dbo].[san_pham];
DELETE FROM [dbo].[danh_muc];
DELETE FROM [dbo].[thuong_hieu];
DELETE FROM [dbo].[kieu_dang];
DELETE FROM [dbo].[chat_lieu];
DELETE FROM [dbo].[mau_sac];
DELETE FROM [dbo].[kich_thuoc];
DELETE FROM [dbo].[phieu_giam_gia];
DELETE FROM [dbo].[nhan_vien];
DELETE FROM [dbo].[khach_hang];
DELETE FROM [dbo].[vai_tro];
GO

-- 2. Đưa bộ đếm Identity về 0 cho tất cả các bảng
DBCC CHECKIDENT ('[dbo].[vai_tro]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[danh_muc]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[thuong_hieu]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[kieu_dang]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[chat_lieu]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[mau_sac]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[kich_thuoc]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[san_pham]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[san_pham_chi_tiet]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[hinh_anh]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[phieu_giam_gia]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[khach_hang]', RESEED, 0);
DBCC CHECKIDENT ('[dbo].[nhan_vien]', RESEED, 0);
GO

-- 3. Chèn Vai trò
INSERT INTO [dbo].[vai_tro] ([ma], [ten], [ngay_tao], [xoa_mem]) VALUES 
('ADMIN', N'Quản trị viên', GETDATE(), 0),
('STAFF', N'Nhân viên bán hàng', GETDATE(), 0);
GO

-- 4. Chèn Thuộc tính sản phẩm
INSERT INTO [dbo].[danh_muc] ([ten_danh_muc], [ngay_tao], [xoa_mem]) VALUES (N'Giày Bóng Rổ', GETDATE(), 0), (N'Giày Chạy Bộ', GETDATE(), 0), (N'Giày Thời Trang', GETDATE(), 0);
INSERT INTO [dbo].[thuong_hieu] ([ten_thuong_hieu], [ngay_tao], [xoa_mem]) VALUES (N'Nike', GETDATE(), 0), (N'Adidas', GETDATE(), 0), (N'Puma', GETDATE(), 0), (N'Jordan', GETDATE(), 0);
INSERT INTO [dbo].[kieu_dang] ([ten_kieu_dang], [ngay_tao], [xoa_mem]) VALUES (N'Cổ Cao', GETDATE(), 0), (N'Cổ Thấp', GETDATE(), 0);
INSERT INTO [dbo].[chat_lieu] ([ten_chat_lieu], [ngay_tao], [xoa_mem]) VALUES (N'Da Thuộc', GETDATE(), 0), (N'Vải Mesh', GETDATE(), 0);
INSERT INTO [dbo].[mau_sac] ([ten_mau_sac], [ma_mau], [ngay_tao], [xoa_mem]) VALUES (N'Đỏ', '#FF0000', GETDATE(), 0), (N'Đen', '#000000', GETDATE(), 0), (N'Trắng', '#FFFFFF', GETDATE(), 0);
INSERT INTO [dbo].[kich_thuoc] ([ten_kich_thuoc], [ngay_tao], [xoa_mem]) VALUES (N'40', GETDATE(), 0), (N'41', GETDATE(), 0), (N'42', GETDATE(), 0);
GO

-- 5. Chèn Sản phẩm
INSERT INTO [dbo].[san_pham] ([ma_san_pham], [ten_san_pham], [mo_ta], [id_danh_muc], [id_thuong_hieu], [id_kieu_dang], [id_chat_lieu], [duong_dan_anh], [ngay_tao], [xoa_mem]) VALUES 
('SP001', N'Nike Air Jordan 1 High OG', N'Đôi giày huyền thoại của Jordan Brand.', 1, 4, 1, 1, '64323da0-3b87-49d1-851b-4944609d3b11_download (1).webp', GETDATE(), 0),
('SP002', N'Adidas Ultraboost 22', N'Giày chạy bộ êm ái nhất của Adidas.', 2, 2, 2, 2, 'b26d02a0-dfc3-4120-9e89-48eaca0373dc_download (2).webp', GETDATE(), 0);
GO

-- 6. Chèn Biến thể chi tiết
INSERT INTO [dbo].[san_pham_chi_tiet] ([id_san_pham], [id_mau_sac], [id_kich_thuoc], [gia_ban], [so_ton_kho], [ma_san_pham_chi_tiet], [trang_thai], [ngay_tao]) VALUES 
(1, 1, 1, 4500000, 50, 'SPCT001', N'Đang kinh doanh', GETDATE()), 
(1, 2, 2, 4650000, 30, 'SPCT002', N'Đang kinh doanh', GETDATE()),
(2, 3, 3, 3500000, 100, 'SPCT003', N'Đang kinh doanh', GETDATE());
GO

-- 7. Chèn Hình ảnh (Album theo màu)
INSERT INTO [dbo].[hinh_anh] ([id_san_pham], [id_san_pham_chi_tiet], [duong_dan], [la_anh_dai_dien], [ngay_tao]) VALUES 
(1, 1, '64323da0-3b87-49d1-851b-4944609d3b11_download (1).webp', 1, GETDATE()),
(1, 2, '64323da0-3b87-49d1-851b-4944609d3b11_download (1).webp', 0, GETDATE()),
(2, 3, 'b26d02a0-dfc3-4120-9e89-48eaca0373dc_download (2).webp', 1, GETDATE());
GO

-- 8. Chèn Người dùng & Voucher
INSERT INTO [dbo].[phieu_giam_gia] ([ma_phieu], [ten_phieu], [gia_tri_giam], [gia_tri_toi_thieu], [hinh_thuc_giam], [loai], [so_luong], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai], [ngay_tao], [xoa_mem]) VALUES ('KM001', N'Chào Mừng 2024', 50000, 200000, N'VNĐ', 0, 100, '2024-01-01', '2024-12-31', 1, GETDATE(), 0);
INSERT INTO [dbo].[khach_hang] ([ma_khach_hang], [ten_day_du], [email], [mat_khau], [so_dien_thoai], [gioi_tinh], [ngay_tao], [xoa_mem]) VALUES ('KH001', N'Nguyễn Văn A', 'customer@gmail.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgNI9dgR663q3LMT/u2g89R99G6u', '0987654321', N'Nam', GETDATE(), 0);
INSERT INTO [dbo].[nhan_vien] ([ma_nhan_vien], [ten_day_du], [email], [mat_khau], [so_dien_thoai], [trang_thai], [id_vai_tro], [ngay_tao], [xoa_mem]) VALUES ('NV001', N'Admin', 'admin@gmail.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgNI9dgR663q3LMT/u2g89R99G6u', '0999999999', N'Đang làm việc', 1, GETDATE(), 0);
GO

-- 9. QUAN TRỌNG: Đồng bộ Identity về giá trị lớn nhất hiện tại
DECLARE @max_hinh_anh INT = (SELECT ISNULL(MAX(id), 0) FROM [hinh_anh]);
DBCC CHECKIDENT ('[dbo].[hinh_anh]', RESEED, @max_hinh_anh);

DECLARE @max_spct INT = (SELECT ISNULL(MAX(id), 0) FROM [san_pham_chi_tiet]);
DBCC CHECKIDENT ('[dbo].[san_pham_chi_tiet]', RESEED, @max_spct);

DECLARE @max_sp INT = (SELECT ISNULL(MAX(id), 0) FROM [san_pham]);
DBCC CHECKIDENT ('[dbo].[san_pham]', RESEED, @max_sp);
GO
