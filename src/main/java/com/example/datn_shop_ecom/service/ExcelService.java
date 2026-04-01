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
}
