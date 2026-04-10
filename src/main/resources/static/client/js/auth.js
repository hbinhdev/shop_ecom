/**
 * PeakSneaker - Global Auth & Cart Utilities
 * Runs on every page via main layout
 */

/* ===== AUTH STATE ===== */
function getUser() {
    try { return JSON.parse(localStorage.getItem('pk_user')); } catch { return null; }
}
function getCart() {
    try { return JSON.parse(localStorage.getItem('pk_cart') || '[]'); } catch { return []; }
}
function saveCart(cart) {
    localStorage.setItem('pk_cart', JSON.stringify(cart));
    updateCartBadge();
}

/* ===== CART BADGE ===== */
function updateCartBadge() {
    const cart = getCart();
    const total = cart.reduce((s, i) => s + (i.soLuong || 1), 0);
    const badge = document.getElementById('cartBadge');
    if (badge) {
        badge.textContent = total;
        badge.style.display = total > 0 ? 'block' : 'none';
    }
}

/* ===== NAVBAR AUTH STATE ===== */
function updateNavbar() {
    const user = getUser();
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const userDropdown = document.getElementById('userDropdown');
    const navUserName = document.getElementById('navUserName');
    const navAvatarInitials = document.getElementById('navAvatarInitials');

    if (user && user.id) {
        // Đã đăng nhập
        if (loginBtn) loginBtn.style.setProperty('display', 'none', 'important');
        if (registerBtn) registerBtn.style.setProperty('display', 'none', 'important');
        if (userDropdown) userDropdown.classList.remove('d-none');
        if (navUserName) navUserName.textContent = user.hoTen || 'Tài khoản';
        if (navAvatarInitials) {
            navAvatarInitials.textContent = (user.hoTen || 'A')[0].toUpperCase();
        }
    } else {
        // Chưa đăng nhập
        if (loginBtn) loginBtn.style.removeProperty('display');
        if (registerBtn) registerBtn.style.removeProperty('display');
        if (userDropdown) userDropdown.classList.add('d-none');
    }
}

/* ===== LOGOUT ===== */
function logoutNav(event) {
    event.preventDefault();
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        localStorage.removeItem('pk_user');
        window.location.href = '/';
    }
}

/* ===== ADD TO CART (Global - called from product pages) ===== */
function addToCart(item) {
    /**
     * item = { spctId, ten, anh, gia, mauSac, kichThuoc, soLuong }
     */
    let cart = getCart();
    const existing = cart.find(i => i.spctId === item.spctId);
    if (existing) {
        existing.soLuong = (existing.soLuong || 1) + (item.soLuong || 1);
    } else {
        cart.push({ ...item, soLuong: item.soLuong || 1 });
    }
    saveCart(cart);
    showCartToast(item.ten);
}

/* ===== CART TOAST ===== */
function showCartToast(tenSanPham) {
    // Create toast if not exists
    if (!document.getElementById('addCartToast')) {
        const toastHtml = `
        <div id="addCartToast" class="toast-cart-global" style="
            position:fixed; bottom:24px; right:24px; z-index:9999;
            background:white; border-radius:16px; padding:14px 20px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.12); border:1px solid #f0f0f0;
            display:flex; align-items:center; gap:12px; max-width:320px;
            transform:translateY(100px); opacity:0; transition:all 0.35s cubic-bezier(0.175,0.885,0.32,1.275);
        ">
            <div style="width:40px;height:40px;background:#27ae60;border-radius:50%;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                <i class="bi bi-cart-check-fill text-white"></i>
            </div>
            <div>
                <div class="fw-bold small">Đã thêm vào giỏ hàng!</div>
                <div class="small text-muted" id="toastProductName"></div>
            </div>
            <a href="/gio-hang" class="btn btn-sm btn-outline-primary rounded-pill ms-2 flex-shrink-0" style="font-size:0.75rem;">Xem giỏ</a>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', toastHtml);
    }
    const toast = document.getElementById('addCartToast');
    const nameEl = document.getElementById('toastProductName');
    if (nameEl) nameEl.textContent = tenSanPham ? tenSanPham.substring(0, 35) : '';
    toast.style.transform = 'translateY(0)';
    toast.style.opacity = '1';
    clearTimeout(window._toastTimeout);
    window._toastTimeout = setTimeout(() => {
        toast.style.transform = 'translateY(100px)';
        toast.style.opacity = '0';
    }, 3000);
}

/* ===== INIT ===== */
document.addEventListener('DOMContentLoaded', () => {
    updateNavbar();
    updateCartBadge();
});
