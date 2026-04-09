package com.example.datn_shop_ecom.controller.client;

import com.example.datn_shop_ecom.entity.SanPham;
import com.example.datn_shop_ecom.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class ClientController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private PhieuGiamGiaService pggService;

    @Autowired
    private DanhMucService danhMucService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @Autowired
    private ChatLieuService chatLieuService;

    @Autowired
    private KieuDangService kieuDangService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Trang chủ - PeakSneaker");
        model.addAttribute("latestProducts", sanPhamService.findAllByXoaMemFalse().stream().limit(8).collect(java.util.stream.Collectors.toList()));
        model.addAttribute("hotVouchers", pggService.findAllByXoaMemFalse().stream().limit(4).collect(java.util.stream.Collectors.toList()));
        return "client/home/index";
    }

    @GetMapping("/san-pham")
    public String shop(Model model, 
                       @RequestParam(required = false) Long danhMuc,
                       @RequestParam(required = false) Long thuongHieu,
                       @RequestParam(required = false) String search) {
        model.addAttribute("pageTitle", "Cửa hàng - PeakSneaker");
        
        
        model.addAttribute("danhMucs", danhMucService.findAll());
        model.addAttribute("thuongHieus", thuongHieuService.findAll());
        model.addAttribute("chatLieus", chatLieuService.findAll());
        model.addAttribute("kieuDangs", kieuDangService.findAll());
        
        
        model.addAttribute("products", sanPhamService.findAllByXoaMemFalse());
        
        return "client/san-pham/index";
    }

    @Autowired
    private SanPhamChiTietService spctService;

    @GetMapping("/san-pham/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SanPham sp = sanPhamService.findById(id);
        model.addAttribute("pageTitle", sp.getTenSanPham() + " - PeakSneaker");
        model.addAttribute("p", sp);
        
        
        java.util.List<com.example.datn_shop_ecom.entity.SanPhamChiTiet> variants = spctService.findBySanPhamId(id);
        model.addAttribute("variants", variants);
        
        
        model.addAttribute("uniqueColors", variants.stream()
            .map(v -> v.getMauSac())
            .distinct()
            .collect(java.util.stream.Collectors.toList()));
            
        model.addAttribute("uniqueSizes", variants.stream()
            .map(v -> v.getKichThuoc())
            .distinct()
            .collect(java.util.stream.Collectors.toList()));
        
        
        model.addAttribute("relatedProducts", sanPhamService.findAllByXoaMemFalse().stream().limit(4).collect(java.util.stream.Collectors.toList()));
        return "client/san-pham/detail";
    }

    @GetMapping("/ve-chung-toi")
    public String aboutUs(Model model) {
        model.addAttribute("pageTitle", "Về chúng tôi - PeakSneaker");
        return "client/ve-chung-toi";
    }

    @GetMapping("/phieu-giam-gia")
    public String vouchers(Model model) {
        model.addAttribute("pageTitle", "Phiếu giảm giá HOT - PeakSneaker");
        return "client/phieu-giam-gia";
    }

    @GetMapping("/tra-cuu")
    public String trackOrder(Model model) {
        model.addAttribute("pageTitle", "Tra cứu đơn hàng - PeakSneaker");
        return "client/tra-cuu";
    }
}

