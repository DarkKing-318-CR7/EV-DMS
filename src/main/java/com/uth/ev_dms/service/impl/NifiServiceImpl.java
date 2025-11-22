package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.service.vm.NifiService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NifiServiceImpl implements NifiService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String NIFI_URL = "http://localhost:9090/evdms";

    @Override
    public void sendToNifi(Object payload) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> entity = new HttpEntity<>(payload, headers);

            restTemplate.exchange(
                    NIFI_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("✔ Sent to NiFi successfully");

        } catch (Exception ex) {
            System.out.println("❌ NiFi ERROR: " + ex.getMessage());
        }
    }
}
