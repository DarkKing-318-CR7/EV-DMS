package com.uth.ev_dms.service.dto;

import lombok.*;

@Getter @Setter

public class TrimPricingForm {

    private Long trimId;

    // readonly để show lên form
    private String trimName;

    // editable
    private Integer basePriceVnd;
    private String currency;

    // có thể mở rộng sau này: effectiveDate, regionCode, etc.

    public Long getTrimId() {
        return trimId;
    }

    public void setTrimId(Long trimId) {
        this.trimId = trimId;
    }

    public String getTrimName() {
        return trimName;
    }

    public void setTrimName(String trimName) {
        this.trimName = trimName;
    }

    public Integer getBasePriceVnd() {
        return basePriceVnd;
    }

    public void setBasePriceVnd(Integer basePriceVnd) {
        this.basePriceVnd = basePriceVnd;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
