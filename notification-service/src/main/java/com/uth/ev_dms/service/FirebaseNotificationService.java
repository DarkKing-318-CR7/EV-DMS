package com.uth.ev_dms.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.uth.ev_dms.dto.FcmTokenResponse;
import com.uth.ev_dms.notification.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOrderApproved(Long orderId, Long staffId) {

        String url = "http://auth-service/internal/users/" + staffId + "/fcm";

        FcmTokenResponse resp = null;

        try {
            resp = restTemplate.getForObject(url, FcmTokenResponse.class);
        } catch (Exception e) {
            System.out.println("⚠️ Không gọi được auth-service để lấy FCM token.");
            return;
        }

        if (resp == null || resp.getFcmToken() == null || resp.getFcmToken().isBlank()) {
            System.out.println("⚠️ User " + staffId + " chưa có FCM token → bỏ qua.");
            return;
        }

        String token = resp.getFcmToken();

        Message message = Message.builder()
                .setToken(token)
                .putData("title", NotificationTemplate.orderApprovedTitle(orderId))
                .putData("body", NotificationTemplate.orderApprovedBody())
                .putData("orderId", String.valueOf(orderId))
                .putData("type", "ORDER_APPROVED")
                .putData("icon", "/image/icon.png")
                .build();

        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            System.out.println("ℹ️ [MOCK] Đơn hàng " + orderId + " đã duyệt. Mock FCM notification gửi tới user " + staffId + ": " + NotificationTemplate.orderApprovedTitle(orderId));
            return;
        }

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("📲 FCM gửi OK → " + response);
        } catch (Exception e) {
            System.out.println("❌ Lỗi gửi FCM cho user " + staffId);
            e.printStackTrace();
        }
    }
}
