package com.uth.ev_dms.fix.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DealerDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    // thêm field nào inventory-service cần dùng
}