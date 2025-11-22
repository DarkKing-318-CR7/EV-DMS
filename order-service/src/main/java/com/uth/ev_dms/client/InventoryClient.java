package com.uth.ev_dms.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    private final RestTemplate rest = new RestTemplate();

    @Value("${inventory.service.url}")
    private String baseUrl;

    public void allocate(Long orderId) {
        rest.postForObject(baseUrl + "/inventory/allocate/" + orderId, null, Void.class);
    }

    public void release(Long orderId) {
        rest.postForObject(baseUrl + "/inventory/release/" + orderId, null, Void.class);
    }

    public void ship(Long orderId) {
        rest.postForObject(baseUrl + "/inventory/ship/" + orderId, null, Void.class);
    }
}
