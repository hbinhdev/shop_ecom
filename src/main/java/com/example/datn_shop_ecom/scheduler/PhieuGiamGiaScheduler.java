package com.example.datn_shop_ecom.scheduler;

import com.example.datn_shop_ecom.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhieuGiamGiaScheduler {

    private final PhieuGiamGiaService pggService;

    /**
     * Tự động cập nhật trạng thái phiếu giảm giá mỗi phút để đảm bảo tính chính xác theo thời gian.
     */
    @Scheduled(fixedRate = 60000)
    public void updateCouponStatuses() {
        try {
            pggService.updateAllStatuses();
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái phiếu giảm giá: {}", e.getMessage());
        }
    }
}
