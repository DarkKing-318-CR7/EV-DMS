package com.uth.ev_dms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleDto {

    private Long id;

    private String modelCode;
    private String modelName;
    private String brand;
    private String bodyType;

    // trạng thái thương mại
    private String regionalStatus;
    private String salesNote;
    private String marketingDesc;

    // có thể thêm basePriceVnd, currency nếu cần
    private Integer basePriceVnd;
    private String currency;
}
