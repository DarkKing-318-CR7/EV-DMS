package com.uth.ev_dms.domain;


import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;


@Entity
@Table(name = "dealers",
        indexes = {@Index(columnList = "code", unique = true),
                @Index(columnList = "region")})
@Getter @Setter @NoArgsConstructor
public class Dealer extends BaseAudit {

    @NotBlank
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 32)
    private String region;            // VD: NORTH, SOUTH...

    @Column(nullable = false)
    private Boolean active = true;
}
