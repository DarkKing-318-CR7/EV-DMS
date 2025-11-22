// Firebase v8 (Service Worker phải dùng v8)
importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-messaging.js');

// Firebase config từ Web App của bạn
firebase.initializeApp({
    apiKey: "AIzaSyButNqr299kVfSX9LI2LP-r5g39hP_6Shc",
    authDomain: "ev-dms.firebaseapp.com",
    projectId: "ev-dms",
    storageBucket: "ev-dms.firebasestorage.app",
    messagingSenderId: "388279030110",
    appId: "1:388279030110:web:e2364ce17412ec324ebfcf",
    measurementId: "G-WGCFZR6263"
});

// Lấy messaging
const messaging = firebase.messaging();

// Nhận background notification (browser đóng/tab ẩn)
messaging.setBackgroundMessageHandler(function(payload) {
    console.log("[SW] Background message:", payload);

    const title = payload.data.title || "EV-DMS";
    const options = {
        body: payload.data.body || "",
        icon: "/image/icon.png",
        data: payload.data
    };

    return self.registration.showNotification(title, options);
});

// Khi user click vào notification
self.addEventListener("notificationclick", function (event) {
    console.log("[SW] Notification click:", event);

    event.notification.close();

    const url = "/evm/orders/" + (event.notification.data?.orderId || "");

    event.waitUntil(
        clients.matchAll({ type: "window" }).then(windowClients => {
            for (let client of windowClients) {
                if (client.url.includes("/evm") && "focus" in client) return client.focus();
            }
            return clients.openWindow(url);
        })
    );
});
