package com.uth.ev_dms.service.dto;
import lombok.*;

@Getter @Setter

public class TransferToDealerForm {
    private Long inventoryId;
    private String vehicleName;
    private String trimName;
    private Integer availableQty;

    // input
    private Long dealerId;
    private Integer quantity;
    private String note;

    // getters/setters
    // ...
}

