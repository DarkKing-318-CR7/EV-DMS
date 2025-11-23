package com.uth.ev_dms.service.dto;

import lombok.*;

@Setter @Getter
public class TrimCommercialForm {
    private Long id;

    // read-only spec:
    private String trimName;
    private String drive;
    private Integer batterykwh;
    private Integer powerHp;
    private Integer rangeKm;

    // editable by EVM:
    private String regionalName;     // tên bán tại khu vực nếu khác trimName
    private String availabilityNote; // ghi chú bán hàng
    private Boolean available;       // trim này có được bán ở khu vực hay không

    // getters/setters
}
