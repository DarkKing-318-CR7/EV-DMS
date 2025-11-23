package com.uth.ev_dms.client;

import com.uth.ev_dms.client.dto.OrderSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(
        name = "order-service",
        url = "${order.service.url}"
)
public interface OrderClient {

    @GetMapping("/api/orders")
    List<OrderSummaryDto> listOrders(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to
    );
}
