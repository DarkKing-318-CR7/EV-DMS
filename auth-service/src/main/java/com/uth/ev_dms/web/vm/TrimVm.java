package com.uth.ev_dms.web.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrimVm {

    private Long id;

    private Long vehicleId;
    private String vehicleModelCode;
    private String vehicleModelName;

    private String trimName;
    private String drive;
    private Integer batterykwh;
    private Integer powerHp;
    private Integer rangeKm;

    private String regionalName;
    private String availabilityNote;
    private Boolean available;

    private Integer basePriceVnd;
    private String currency;
}
