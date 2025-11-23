package com.uth.ev_dms.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerFeignDto {

    private Long id;
    private String ten;
    private String sdt;
    private String email;
    private String diachi;

    private String status;
    private Instant createdAt;
    private LocalDateTime ngaytao;
}
