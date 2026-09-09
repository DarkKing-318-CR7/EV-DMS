package com.uth.ev_dms.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return; // đã init rồi
        }

        try {
            ClassPathResource resource =
                    new ClassPathResource("firebase-service-account.json");

            if (!resource.exists()) {
                System.out.println("⚠️ [FirebaseConfig] File 'firebase-service-account.json' không tồn tại. Firebase chạy ở chế độ MOCK (không gửi push thật).");
                return;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized thành công");
            }
        } catch (Exception e) {
            System.err.println("⚠️ [FirebaseConfig] Không thể khởi tạo Firebase: " + e.getMessage());
        }
    }
}
