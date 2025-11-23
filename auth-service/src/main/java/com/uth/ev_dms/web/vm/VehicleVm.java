package com.uth.ev_dms.web.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleVm {

    private Long id;
    private String modelCode;
    private String modelName;
    private String brand;
    private String bodyType;
    private Integer warrantyMonths;

    private String regionalStatus;
    private String salesNote;
    private String marketingDesc;
}
