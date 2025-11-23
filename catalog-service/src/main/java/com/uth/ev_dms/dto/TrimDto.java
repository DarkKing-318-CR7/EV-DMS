package com.uth.ev_dms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrimDto {

    private Long id;

    private Long vehicleId;
    private String vehicleModelCode;
    private String vehicleModelName;

    private String trimName;
    private String drive;
    private Integer Batterykwh;
    private Integer powerHp;
    private Integer rangeKm;

    // Thông tin thương mại
    private String regionalName;
    private String availabilityNote;
    private Boolean available;

    // Giá cơ bản hiện tại (nếu cần)
    private Integer basePriceVnd;
    private String currency;
}
