package com.uth.ev_dms.client.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter @Setter
public class InventoryDto {

    private Long id;

    private Long dealerId;
    private String dealerName;

    private Long branchId;
    private String branchName;

    private Long trimId;
    private String trimName;
    private String modelName;

//    private Integer quantityOnHand;
    private Integer reserved;
    private Integer available;

    private String locationType;   // <-- để binding với form.html
    private Integer qtyOnHand;
    private String color;
    private String vin;

    private LocalDateTime updatedAt;
}
