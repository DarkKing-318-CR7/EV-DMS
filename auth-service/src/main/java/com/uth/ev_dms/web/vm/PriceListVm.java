package com.uth.ev_dms.web.vm;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PriceListVm {

    private Long id;
    private Long trimId;
    private String trimName;

    private Integer basePriceVnd;
    private String currency;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;

    private BigDecimal msrp;
}
