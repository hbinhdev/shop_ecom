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
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoCancelPOSInvoices() {
        log.info("--- [SYSTEM] Chức năng tự động hủy đơn POS đã được tạm dừng theo yêu cầu. ---");
        /*
        List<HoaDon> pendingInvoices = hoaDonRepository.findAllPendingPOS(LocalDateTime.now().minusDays(1));
        if (!pendingInvoices.isEmpty()) {
            for (HoaDon hd : pendingInvoices) {
                hd.setTrangThaiHoaDon("DA_HUY");
                hd.setMoTa("Hệ thống tự động hủy đơn chờ cuối ngày (12h đêm)");
                hd.setNgaySuaCuoi(LocalDateTime.now());
                hd.setNguoiSuaCuoi("SYSTEM");
            }
            hoaDonRepository.saveAll(pendingInvoices);
        }
        */
    }
}
