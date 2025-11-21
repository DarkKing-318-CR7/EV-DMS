package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class PromotionApplied {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quoteId;              // Lưu theo ID cho gọn, có thể đổi sang @ManyToOne nếu muốn
    private Long promotionId;

    private BigDecimal discountAmount; // Số tiền giảm bởi promotion này
    private LocalDateTime appliedAt;
}
