// static/js/main.js
document.addEventListener('DOMContentLoaded', () => {
    // Fallback active menu theo URL hiện tại (nếu không set activePage)
    const path = location.pathname.replace(/\/+$/, '');
    document.querySelectorAll('.menu a').forEach(a => {
        const href = a.getAttribute('href').replace(/\/+$/, '');
        if (href && path.startsWith(href)) a.classList.add('active');
    });
});
