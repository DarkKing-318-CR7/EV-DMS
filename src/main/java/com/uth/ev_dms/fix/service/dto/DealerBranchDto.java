package com.uth.ev_dms.fix.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter @Setter
public class DealerBranchDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    // tuỳ nhu cầu hiển thị
}