package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String title;
    private String description;
    private Double discountRate;

    // Điều kiện áp dụng (null = không ràng buộc điều kiện đó)
    private String region;            // "north" | "central" | "south" (ví dụ)
    private Long dealerId;            // Áp dụng cho một đại lý cụ thể, nếu null = tất cả
    private Long vehicleTrimId;       // Áp dụng cho 1 trim, nếu null = tất cả

    // Mức giảm
    private BigDecimal discountPercent;

    // Thời gian hiệu lực
    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean active;
    private BigDecimal budget;        // Ngân sách tổng (optional)

}
