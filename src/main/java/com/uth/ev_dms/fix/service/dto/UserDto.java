package com.uth.ev_dms.fix.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter @Setter
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Long DealerId;
}
