package com.uth.ev_dms.service.dto;

import java.time.LocalDateTime;

public class InventoryResponse {

    private Long id;
    private Long dealerId;
    private String dealerName;
    private Long branchId;
    private String branchName;
    private Long trimId;
    private String trimName;

    private Integer inStock;
    private Integer reserved;
    private Integer sold;
    private Integer onHand;
    private Integer available;

    private LocalDateTime updatedAt;

    // getters & setters
}
