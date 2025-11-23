package com.uth.ev_dms.service.dto;

import lombok.*;

@Getter @Setter
public class VehicleCommercialForm {
    private Long id;

    // read-only để show trong form:
    private String modelCode;
    private String modelName;
    private String bodyType;
    private Integer warrantyMonths;

    // editable bởi EVM:
    private String regionalStatus;   // AVAILABLE / LIMITED / STOP_SELLING
    private String salesNote;        // ghi chú nội bộ cho dealer
    private String marketingDesc;    // mô tả ngắn cho thị trường

    // getters/setters
}
