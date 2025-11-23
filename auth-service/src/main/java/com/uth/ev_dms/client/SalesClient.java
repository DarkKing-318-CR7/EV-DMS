package com.uth.ev_dms.client;

import com.uth.ev_dms.client.dto.QuoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "sales-service",
        url = "${sales.service.url}"
)
public interface SalesClient {

    @GetMapping("/api/sales/quotes")
    List<QuoteDto> getQuotes(
            @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/api/sales/quotes/{id}")
    QuoteDto getQuote(@PathVariable("id") Long id);
}
