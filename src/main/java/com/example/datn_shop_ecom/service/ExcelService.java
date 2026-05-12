package com.example.datn_shop_ecom.service;

import com.example.datn_shop_ecom.entity.HoaDon;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream exportInvoicesToExcel(List<HoaDon> invoices) throws IOException {
        String[] columns = {"STT", "Mã hóa đơn", "Ngày tạo", "Khách hàng", "Số điện thoại", "Tổng tiền", "Loại đơn", "Trạng thái"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (HoaDon hd : invoices) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(hd.getMaHoaDon());
                row.createCell(2).setCellValue(hd.getNgayTao() != null ? hd.getNgayTao().format(formatter) : "N/A");
                row.createCell(3).setCellValue(hd.getKhachHang() != null ? hd.getKhachHang().getTenDayDu() : "Khách lẻ");
                row.createCell(4).setCellValue(hd.getSoDienThoaiNguoiNhan() != null ? hd.getSoDienThoaiNguoiNhan() : "N/A");
                
                row.createCell(5).setCellValue(hd.getTongTienAfterGiam() != null ? hd.getTongTienAfterGiam().doubleValue() : 0.0);
                
                String loai = "1".equals(hd.getLoaiHoaDon()) || "TAI_CUA_HANG".equals(hd.getLoaiHoaDon()) ? "Tại quầy" : "Giao hàng";
                row.createCell(6).setCellValue(loai);
                
                row.createCell(7).setCellValue(hd.getTrangThaiHoaDon());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    public ByteArrayInputStream exportThuocTinhToExcel(
            List<com.example.datn_shop_ecom.entity.MauSac> listMauSac,
            List<com.example.datn_shop_ecom.entity.KichThuoc> listKichThuoc,
            List<com.example.datn_shop_ecom.entity.DanhMuc> listDanhMuc,
            List<com.example.datn_shop_ecom.entity.ThuongHieu> listThuongHieu,
            List<com.example.datn_shop_ecom.entity.KieuDang> listKieuDang,
            List<com.example.datn_shop_ecom.entity.ChatLieu> listChatLieu
    ) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            Sheet sheetMauSac = workbook.createSheet("Màu sắc");
            createHeader(sheetMauSac, headerCellStyle, "STT", "Tên màu sắc", "Trạng thái");
            int r = 1;
            for (com.example.datn_shop_ecom.entity.MauSac m : listMauSac) {
                Row row = sheetMauSac.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenMauSac());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetMauSac, 3);

            Sheet sheetKichThuoc = workbook.createSheet("Kích thước");
            createHeader(sheetKichThuoc, headerCellStyle, "STT", "Kích thước", "Trạng thái");
            r = 1;
            for (com.example.datn_shop_ecom.entity.KichThuoc m : listKichThuoc) {
                Row row = sheetKichThuoc.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenKichThuoc());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetKichThuoc, 3);

            Sheet sheetDanhMuc = workbook.createSheet("Danh mục");
            createHeader(sheetDanhMuc, headerCellStyle, "STT", "Tên danh mục", "Trạng thái");
            r = 1;
            for (com.example.datn_shop_ecom.entity.DanhMuc m : listDanhMuc) {
                Row row = sheetDanhMuc.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenDanhMuc());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetDanhMuc, 3);

            Sheet sheetThuongHieu = workbook.createSheet("Thương hiệu");
            createHeader(sheetThuongHieu, headerCellStyle, "STT", "Tên thương hiệu", "Trạng thái");
            r = 1;
            for (com.example.datn_shop_ecom.entity.ThuongHieu m : listThuongHieu) {
                Row row = sheetThuongHieu.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenThuongHieu());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetThuongHieu, 3);

            Sheet sheetKieuDang = workbook.createSheet("Kiểu dáng");
            createHeader(sheetKieuDang, headerCellStyle, "STT", "Kiểu dáng", "Trạng thái");
            r = 1;
            for (com.example.datn_shop_ecom.entity.KieuDang m : listKieuDang) {
                Row row = sheetKieuDang.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenKieuDang());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetKieuDang, 3);

            Sheet sheetChatLieu = workbook.createSheet("Chất liệu");
            createHeader(sheetChatLieu, headerCellStyle, "STT", "Chất liệu", "Trạng thái");
            r = 1;
            for (com.example.datn_shop_ecom.entity.ChatLieu m : listChatLieu) {
                Row row = sheetChatLieu.createRow(r++);
                row.createCell(0).setCellValue(r - 1);
                row.createCell(1).setCellValue(m.getTenChatLieu());
                row.createCell(2).setCellValue(m.getXoaMem() == null || !m.getXoaMem() ? "Hoạt động" : "Ngừng hoạt động");
            }
            autoSize(sheetChatLieu, 3);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void createHeader(Sheet sheet, CellStyle style, String... cols) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}

