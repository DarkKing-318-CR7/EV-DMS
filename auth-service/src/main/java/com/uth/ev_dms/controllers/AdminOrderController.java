package com.uth.ev_dms.controllers;



import com.uth.ev_dms.client.OrderClient;
import com.uth.ev_dms.client.dto.OrderDetailDto;
import com.uth.ev_dms.client.dto.OrderSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderClient orderClient;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        List<OrderSummaryDto> orders = orderClient.listOrders(q, status, from, to);
        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/orders/orders-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderDetailDto dto = orderClient.getOrder(id);
        model.addAttribute("order", dto);
        return "admin/orders/order-detail";
    }
}
