package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.service.OrderService;
import com.uth.ev_dms.dto.OrderDetailDto;
import com.uth.ev_dms.dto.OrderItemDto;
import com.uth.ev_dms.dto.OrderSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderRepo orderRepo;
    private final OrderService orderService;

    // ============ LIST (dùng cho Admin UI) ============
    @GetMapping
    public List<OrderSummaryDto> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<OrderHdr> all = orderRepo.findAllByOrderByIdDesc();
        Stream<OrderHdr> stream = all.stream();

        if (q != null && !q.isBlank()) {
            String kw = q.trim().toLowerCase();
            stream = stream.filter(o ->
                    String.valueOf(o.getId()).toLowerCase().contains(kw) ||
                            (o.getDealerId() != null && String.valueOf(o.getDealerId()).toLowerCase().contains(kw)) ||
                            (o.getCustomerId() != null && String.valueOf(o.getCustomerId()).toLowerCase().contains(kw)) ||
                            (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(kw))
            );
        }

        if (status != null && !status.isBlank()) {
            try {
                var st = Enum.valueOf(com.uth.ev_dms.domain.OrderStatus.class, status.trim());
                stream = stream.filter(o -> o.getStatus() == st);
            } catch (Exception ignored) {}
        }

        if (from != null) {
            stream = stream.filter(o -> o.getCreatedAt() != null &&
                    !o.getCreatedAt().toLocalDate().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(o -> o.getCreatedAt() != null &&
                    !o.getCreatedAt().toLocalDate().isAfter(to));
        }

        return stream
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    // ============ DETAIL ============
    @GetMapping("/{id}")
    public OrderDetailDto detail(@PathVariable Long id) {
        OrderHdr order = orderService.findById(id);
        List<OrderItem> items = orderService.findItems(id);

        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setDealerId(order.getDealerId());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDepositAmount(order.getDepositAmount());
        dto.setPaidAmount(order.getPaidAmount());
        dto.setBalanceAmount(order.getBalanceAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDto> itemDtos = items.stream().map(this::toItemDto).toList();
        dto.setItems(itemDtos);

        return dto;
    }

    // ============ MAPPERS ============
    private OrderSummaryDto toSummaryDto(OrderHdr o) {
        OrderSummaryDto dto = new OrderSummaryDto();
        dto.setId(o.getId());
        dto.setDealerId(o.getDealerId());
        dto.setCustomerId(o.getCustomerId());
        dto.setCustomerName(o.getCustomerName());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setStatus(o.getStatus());
        dto.setCreatedAt(o.getCreatedAt());
        return dto;
    }

    private OrderItemDto toItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setTrimId(item.getTrimId());
        dto.setVehicleId(item.getVehicleId());
        dto.setQty(item.getQty());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getUnitPrice());
        return dto;
    }
}
