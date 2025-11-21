package com.uth.ev_dms.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.uth.ev_dms.notification.NotificationTemplate;
import com.uth.ev_dms.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

    private final UserRepo userRepo;

    public void sendOrderApproved(Long orderId, Long staffId) {

        String token = userRepo.findFcmTokenByUserId(staffId);

        if (token == null || token.isBlank()) {
            System.out.println("⚠️ User " + staffId + " chưa có FCM token → bỏ qua gửi thông báo.");
            return;
        }

        // 🔥 Quan trọng: KHÔNG được dùng .setNotification() cho Web + ServiceWorker
        // Chỉ dùng DATA message để SW đọc payload.data

        Message message = Message.builder()
                .setToken(token)
                .putData("title", NotificationTemplate.orderApprovedTitle(orderId))
                .putData("body", NotificationTemplate.orderApprovedBody())
                .putData("orderId", String.valueOf(orderId))
                .putData("type", "ORDER_APPROVED")
                .putData("icon", "/image/icon.png")      // << MUST HAVE
                .build();


        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("📲 FCM gửi thành công cho user " + staffId + " → " + response);
        } catch (Exception e) {
            System.out.println("❌ Lỗi gửi FCM cho user " + staffId + ", order " + orderId);
            e.printStackTrace();
        }
    }
}
