package com.uth.ev_dms.client.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrimFeignDto {

    private Long id;

    private Long vehicleId;
    private String vehicleModelCode;
    private String vehicleModelName;

    private String trimName;
    private String drive;       // enum DriveType -> String
    private Integer batterykwh; // batteryKWh
    private Integer powerHp;
    private Integer rangeKm;

    private String regionalName;
    private String availabilityNote;
    private Boolean available;

    private Integer basePriceVnd;
    private String currency;
}
