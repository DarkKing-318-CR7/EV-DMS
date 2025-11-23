package com.uth.ev_dms.service.dto;

import lombok.*;

@Getter @Setter

public class AdjustInventoryForm {
    private Long inventoryId;
    private String vehicleName;  // để hiển thị
    private String trimName;     // để hiển thị
    private Integer currentQty;  // để hiển thị

    // input
    private Integer deltaQty;    // + hoặc -
    private String reason;

    // getters/setters
    // ...
}

