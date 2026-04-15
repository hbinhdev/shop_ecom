package com.example.datn_shop_ecom.controller.client;

import com.example.datn_shop_ecom.entity.DiaChi;
import com.example.datn_shop_ecom.entity.KhachHang;
import com.example.datn_shop_ecom.entity.SanPham;
import com.example.datn_shop_ecom.service.*;
import com.example.datn_shop_ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private KhachHangService khachHangService;

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Trang chủ - PeakSneaker");
        model.addAttribute("latestProducts", sanPhamService.getLatestProducts(8));
        model.addAttribute("bestSellers", sanPhamService.getTopBestSellers(8));
        model.addAttribute("hotVouchers", pggService.findAllByXoaMemFalse().stream().limit(4).collect(java.util.stream.Collectors.toList()));
        return "client/home/index";
    }

    @Autowired
    private MauSacRepository mauSacRepo;

    @Autowired
    private KichThuocRepository kichThuocRepo;

    @Autowired
    private KieuDangService kieuDangService;

    @Autowired
    private ChatLieuService chatLieuService;

    @GetMapping("/san-pham")
    public String shop(Model model, 
                       @RequestParam(required = false) Long danhMuc,
                       @RequestParam(required = false) Long thuongHieu,
                       @RequestParam(required = false) Long kieuDang,
                       @RequestParam(required = false) Long chatLieu,
                       @RequestParam(required = false) Long mauSac,
                       @RequestParam(required = false) Long kichThuoc,
                       @RequestParam(required = false, name = "search") String search,
                       @RequestParam(required = false, defaultValue = "newest") String sort) {
        model.addAttribute("pageTitle", "Cửa hàng - PeakSneaker");
        
        // Load data for sidebar filters
        model.addAttribute("danhMucs", danhMucService.findAll());
        model.addAttribute("thuongHieus", thuongHieuService.findAll());
        model.addAttribute("kieuDangs", kieuDangService.findAll());
        model.addAttribute("chatLieus", chatLieuService.findAll());
        model.addAttribute("mauSacs", mauSacRepo.findAllByXoaMemFalse());
        model.addAttribute("kichThuocs", kichThuocRepo.findAllByXoaMemFalse());
        
        // Keep selected values in UI
        model.addAttribute("selectedDanhMuc", danhMuc);
        model.addAttribute("selectedThuongHieu", thuongHieu);
        model.addAttribute("selectedKieuDang", kieuDang);
        model.addAttribute("selectedChatLieu", chatLieu);
        model.addAttribute("selectedMauSac", mauSac);
        model.addAttribute("selectedKichThuoc", kichThuoc);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedSort", sort);
        
        // Filtered products
        model.addAttribute("products", sanPhamService.findByClientFilters(search, danhMuc, thuongHieu, kieuDang, chatLieu, mauSac, kichThuoc, sort));
        
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
        model.addAttribute("vouchers", pggService.findAllByXoaMemFalse());
        return "client/phieu-giam-gia";
    }

    @GetMapping("/tra-cuu")
    public String trackOrder(Model model) {
        model.addAttribute("pageTitle", "Tra cuưu đơn hàng - PeakSneaker");
        return "client/tra-cuu/index";
    }

    @GetMapping("/dang-nhap")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Đăng nhập - PeakSneaker");
        return "client/dang-nhap";
    }

    @GetMapping("/gio-hang")
    public String cart(Model model) {
        model.addAttribute("pageTitle", "Giỏ hàng - PeakSneaker");
        return "client/gio-hang/index";
    }

    @GetMapping("/thanh-toan")
    public String checkout(Model model) {
        model.addAttribute("pageTitle", "Thanh toán - PeakSneaker");
        return "client/thanh-toan/index";
    }

    @GetMapping("/tai-khoan")
    public String account(Model model) {
        model.addAttribute("pageTitle", "Tài khoản - PeakSneaker");
        return "client/tai-khoan/index";
    }

    // ===== ĐĂNG KÝ TÀI KHOẢN =====
    @GetMapping("/dang-ky")
    public String registerForm(Model model) {
        model.addAttribute("pageTitle", "Đăng ký tài khoản - PeakSneaker");
        KhachHang khachHang = new KhachHang();
        khachHang.getDanhSachDiaChi().add(new DiaChi());
        model.addAttribute("khachHang", khachHang);
        return "client/dang-ky";
    }

    @PostMapping("/dang-ky")
    public String register(
            @ModelAttribute("khachHang") KhachHang khachHang,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            khachHangService.registerKhachHang(khachHang);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng kiểm tra Email để nhận mật khẩu đăng nhập.");
            return "redirect:/dang-nhap";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Đăng ký tài khoản - PeakSneaker");
            model.addAttribute("khachHang", khachHang);
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "client/dang-ky";
        }
    }
}

