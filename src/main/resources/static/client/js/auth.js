/**
 * PeakSneaker - Global Auth & Cart Utilities
 * Improved for functional reliability
 */

/* ===== AUTH STATE ===== */
function getUser() {
    try { return JSON.parse(localStorage.getItem('pk_user')); } catch { return null; }
}

function getCart() {
    try { 
        const cart = JSON.parse(localStorage.getItem('pk_cart') || '[]'); 
        return Array.isArray(cart) ? cart : [];
    } catch (e) { 
        console.error("Cart data corrupted, resetting...", e);
        return []; 
    }
}

function saveCart(cart) {
    try {
        localStorage.setItem('pk_cart', JSON.stringify(cart));
        updateCartBadge();
        return true;
    } catch (e) {
        console.error("Failed to save cart to localStorage", e);
        alert("Không thể lưu giỏ hàng. Vui lòng kiểm tra dung lượng trình duyệt!");
        return false;
    }
}

/* ===== CART BADGE ===== */
function updateCartBadge() {
    const cart = getCart();
    const total = cart.reduce((s, i) => s + (parseInt(i.soLuong) || 1), 0);
    const badge = document.getElementById('cartBadge');
    if (badge) {
        badge.textContent = total;
        badge.style.display = total > 0 ? 'flex' : 'none';
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
        if (loginBtn) loginBtn.style.setProperty('display', 'none', 'important');
        if (registerBtn) registerBtn.style.setProperty('display', 'none', 'important');
        if (userDropdown) userDropdown.classList.remove('d-none');
        if (navUserName) navUserName.textContent = user.hoTen || 'Tài khoản';
        if (navAvatarInitials) {
            navAvatarInitials.textContent = (user.hoTen || 'A')[0].toUpperCase();
        }
    } else {
        if (loginBtn) loginBtn.style.removeProperty('display');
        if (registerBtn) registerBtn.style.removeProperty('display');
        if (userDropdown) userDropdown.classList.add('d-none');
    }
}

/* ===== LOGOUT ===== */
window.logoutNav = function(event) {
    if (event) event.preventDefault();
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        localStorage.removeItem('pk_user');
        window.location.href = '/';
    }
};

/* ===== ADD TO CART (Global) ===== */
window.addToCart = async function(item) {
    if (!item || !item.spctId) {
        console.error("Invalid item passed to addToCart", item);
        return;
    }

    console.log("Adding item to cart:", item);
    const user = getUser();
    
    // Nếu đã đăng nhập, lưu vào DB trước
    if (user && user.id) {
        try {
            const resp = await fetch('/api/client/cart/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ khId: user.id, spctId: item.spctId, soLuong: item.soLuong || 1 })
            });
            const data = await resp.json();
            if (!data.success) console.error("Failed to add to DB cart");
        } catch (e) {
            console.error("Error adding to DB cart", e);
        }
    }

    // Luôn lưu vào LocalStorage để đảm bảo trải nghiệm tức thì (Optimistic UI)
    let cart = getCart();
    const existingIndex = cart.findIndex(i => String(i.spctId) === String(item.spctId));
    if (existingIndex > -1) {
        cart[existingIndex].soLuong = (parseInt(cart[existingIndex].soLuong) || 1) + (parseInt(item.soLuong) || 1);
    } else {
        cart.push({ ...item, soLuong: parseInt(item.soLuong) || 1 });
    }
    
    if (saveCart(cart)) {
        // showCartToast(item.ten); // Disabled as per user request
    }
};

/* ===== SYNC CART WITH DB ===== */
window.syncCartWithDb = async function() {
    const user = getUser();
    if (!user || !user.id) return;

    const cart = getCart();
    
    // 1. Sync local items TO Database
    if (cart.length > 0) {
        try {
            await fetch('/api/client/cart/sync', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ khId: user.id, items: cart.map(i => ({ spctId: i.spctId, soLuong: i.soLuong })) })
            });
            console.log("Cart synced TO database");
        } catch (e) {
            console.error("Sync to DB failed", e);
        }
    }

    // 2. Fetch merged cart FROM Database
    try {
        const resp = await fetch('/api/client/cart/items/' + user.id);
        const dbItems = await resp.json();
        if (Array.isArray(dbItems)) {
            // Cập nhật lại LocalStorage từ DB (DB là nguồn chân lý)
            localStorage.setItem('pk_cart', JSON.stringify(dbItems));
            updateCartBadge();
            console.log("Cart fetched FROM database", dbItems);
        }
    } catch (e) {
        console.error("Fetch from DB failed", e);
    }
}

/* ===== CART TOAST ===== */
function showCartToast(tenSanPham) {
    let toast = document.getElementById('addCartToast');
    if (!toast) {
        const toastHtml = `
        <div id="addCartToast" class="toast-cart-global" style="
            position:fixed; bottom:24px; right:24px; z-index:10000;
            background:white; border-radius:16px; padding:16px 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.15); border:1px solid #f0f0f0;
            display:flex; align-items:center; gap:12px; min-width:280px; max-width:350px;
            transform:translateY(150px); opacity:0; transition:all 0.4s cubic-bezier(0.175,0.885,0.32,1.275);
        ">
            <div style="width:44px;height:44px;background:#27ae60;border-radius:50%;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                <i class="bi bi-cart-check-fill text-white fs-5"></i>
            </div>
            <div style="flex-grow:1;">
                <div class="fw-bold" style="font-size:0.95rem; color:#2c3e50;">Giỏ hàng đã cập nhật!</div>
                <div class="small text-muted text-truncate" style="max-width:180px;" id="toastProductName"></div>
            </div>
            <a href="/gio-hang" class="btn btn-sm btn-primary rounded-pill px-3 shadow-none" style="font-size:0.8rem; font-weight:600;">Xem</a>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', toastHtml);
        toast = document.getElementById('addCartToast');
    }
    
    const nameEl = document.getElementById('toastProductName');
    if (nameEl) nameEl.textContent = tenSanPham || 'Sản phẩm mới';
    
    setTimeout(() => {
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';
    }, 10);

    clearTimeout(window._toastTimeout);
    window._toastTimeout = setTimeout(() => {
        toast.style.transform = 'translateY(150px)';
        toast.style.opacity = '0';
    }, 4000);
}

/* ===== INIT ===== */
document.addEventListener('DOMContentLoaded', () => {
    updateNavbar();
    updateCartBadge();
    
    // Nếu đã đăng nhập, thỉnh thoảng đồng bộ lại với DB
    if (getUser()) {
        syncCartWithDb();
    }
});
