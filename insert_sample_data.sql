-- ==========================================================
-- DỮ LIỆU MẪU CHO CÁC BẢNG THUỘC TÍNH SẢN PHẨM
-- ==========================================================

USE peak_sneaker;
GO

-- 1. Bảng danh_muc
INSERT INTO danh_muc (ten_danh_muc, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Sneaker', 'Admin', GETDATE(), 0),
(N'Giày Chạy Bộ', 'Admin', GETDATE(), 0),
(N'Giày Bóng Rổ', 'Admin', GETDATE(), 0),
(N'Giày Đá Bóng', 'Admin', GETDATE(), 0),
(N'Casual', 'Admin', GETDATE(), 0);
GO

-- 2. Bảng thuong_hieu
INSERT INTO thuong_hieu (ten_thuong_hieu, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Nike', 'Admin', GETDATE(), 0),
(N'Adidas', 'Admin', GETDATE(), 0),
(N'Jordan', 'Admin', GETDATE(), 0),
(N'Puma', 'Admin', GETDATE(), 0),
(N'New Balance', 'Admin', GETDATE(), 0);
GO

-- 3. Bảng kieu_dang
INSERT INTO kieu_dang (ten_kieu_dang, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Cổ Thấp (Low-top)', 'Admin', GETDATE(), 0),
(N'Cổ Cao (High-top)', 'Admin', GETDATE(), 0),
(N'Cổ Lửng (Mid-top)', 'Admin', GETDATE(), 0),
(N'Slip-on', 'Admin', GETDATE(), 0);
GO

-- 4. Bảng chat_lieu
INSERT INTO chat_lieu (ten_chat_lieu, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Da Thuộc', 'Admin', GETDATE(), 0),
(N'Vải Mesh', 'Admin', GETDATE(), 0),
(N'Da Tổng Hợp', 'Admin', GETDATE(), 0),
(N'Vải Canvas', 'Admin', GETDATE(), 0),
(N'Suede (Da lộn)', 'Admin', GETDATE(), 0);
GO

-- 5. Bảng xuat_xu
INSERT INTO xuat_xu (ten_xuat_xu, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Việt Nam', 'Admin', GETDATE(), 0),
(N'Trung Quốc', 'Admin', GETDATE(), 0),
(N'Indonesia', 'Admin', GETDATE(), 0),
(N'Thái Lan', 'Admin', GETDATE(), 0),
(N'USA', 'Admin', GETDATE(), 0);
GO

-- 6. Bảng de_giay
INSERT INTO de_giay (ten_de_giay, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Đế Cao Su', 'Admin', GETDATE(), 0),
(N'Đế Phylon', 'Admin', GETDATE(), 0),
(N'Đế TPU', 'Admin', GETDATE(), 0),
(N'Đế Memory Foam', 'Admin', GETDATE(), 0);
GO

-- 7. Bảng nha_san_xuat
INSERT INTO nha_san_xuat (ten_nha_san_xuat, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Công ty TNHH Nike Việt Nam', 'Admin', GETDATE(), 0),
(N'Adidas Group HQ', 'Admin', GETDATE(), 0),
(N'Puma SE', 'Admin', GETDATE(), 0),
(N'Tập đoàn Sun Group (Phân phối)', 'Admin', GETDATE(), 0);
GO

-- 8. Bảng mau_sac
INSERT INTO mau_sac (ten_mau_sac, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'Trắng', 'Admin', GETDATE(), 0),
(N'Đen', 'Admin', GETDATE(), 0),
(N'Đỏ', 'Admin', GETDATE(), 0),
(N'Xanh dương', 'Admin', GETDATE(), 0),
(N'Xám', 'Admin', GETDATE(), 0),
(N'Vàng', 'Admin', GETDATE(), 0),
(N'Tím', 'Admin', GETDATE(), 0);
GO

-- 9. Bảng kich_thuoc
INSERT INTO kich_thuoc (ten_kich_thuoc, nguoi_tao, ngay_tao, xoa_mem) VALUES 
(N'36', 'Admin', GETDATE(), 0),
(N'37', 'Admin', GETDATE(), 0),
(N'38', 'Admin', GETDATE(), 0),
(N'39', 'Admin', GETDATE(), 0),
(N'40', 'Admin', GETDATE(), 0),
(N'41', 'Admin', GETDATE(), 0),
(N'42', 'Admin', GETDATE(), 0),
(N'43', 'Admin', GETDATE(), 0),
(N'44', 'Admin', GETDATE(), 0);
GO

-- 10. Bảng vai_tro (Nếu chưa có)
IF NOT EXISTS (SELECT * FROM vai_tro WHERE ma = 'ADMIN')
INSERT INTO vai_tro (ma, ten, nguoi_tao, ngay_tao, xoa_mem) VALUES ('ADMIN', N'Quản trị viên', 'System', GETDATE(), 0);
IF NOT EXISTS (SELECT * FROM vai_tro WHERE ma = 'STAFF')
INSERT INTO vai_tro (ma, ten, nguoi_tao, ngay_tao, xoa_mem) VALUES ('STAFF', N'Nhân viên bán hàng', 'System', GETDATE(), 0);
GO

-- 11. Bảng nhan_vien mẫu (Mật khẩu mặc định là 123456)
IF NOT EXISTS (SELECT * FROM nhan_vien WHERE email = 'admin@gmail.com')
INSERT INTO nhan_vien (id_vai_tro, ma_nhan_vien, ten_day_du, email, mat_khau, trang_thai, ngay_tao)
SELECT id, 'NV00001', N'Nguyễn Văn Admin', 'admin@gmail.com', '123456', 'Active', GETDATE() FROM vai_tro WHERE ma = 'ADMIN';
GO
