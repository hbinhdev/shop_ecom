package com.example.datn_shop_ecom.scheduler;

import com.example.datn_shop_ecom.entity.HoaDon;
import com.example.datn_shop_ecom.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private final HoaDonRepository hoaDonRepository;

    /**
     * Tự động hủy toàn bộ các hóa đơn chờ tại quầy (POS) vào lúc 12h đêm hàng ngày.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void autoCancelPOSInvoices() {
        log.info("--- [SYSTEM] Bắt đầu quét và reset hóa đơn POS chờ thanh toán cuối ngày ---");
        
        // Tìm toàn bộ hóa đơn chờ thanh toán tại quầy
        List<HoaDon> pendingInvoices = hoaDonRepository.findAllPendingPOS();
        
        if (!pendingInvoices.isEmpty()) {
            for (HoaDon hd : pendingInvoices) {
                hd.setTrangThaiHoaDon("DA_HUY");
                hd.setMoTa("Hệ thống tự động hủy đơn chờ cuối ngày (12h đêm)");
                hd.setNgaySuaCuoi(LocalDateTime.now());
                hd.setNguoiSuaCuoi("SYSTEM");
                log.info("Đã tự động hủy hóa đơn chờ: {}", hd.getMaHoaDon());
            }
            hoaDonRepository.saveAll(pendingInvoices);
            log.info("Hoàn tất hủy {} hóa đơn POS tồn đọng.", pendingInvoices.size());
        } else {
            log.info("Không có hóa đơn POS nào đang chờ. Hoàn tất reset.");
        }
    }
}
