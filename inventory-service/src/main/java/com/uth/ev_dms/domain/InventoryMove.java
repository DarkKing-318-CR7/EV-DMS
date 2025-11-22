package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_moves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID đại lý (dealer) – lấy từ OrderHdr.dealerId
    private Long dealerId;

    // Nếu sau này cần lưu chi nhánh thì có thể thêm, tạm thời không bắt buộc
    private Long branchId;

    // Trim ID
    private Long trimId;

    // Số lượng thay đổi (reserve / ship / release)
    private Integer qty;

    /**
     * Loại move:
     *  - RESERVE: giữ hàng cho order
     *  - SHIP: giao hàng
     *  - RELEASE: trả lại reserved
     */
    private String type;

    /**
     * Loại tham chiếu:
     *  - ORDER, MANUAL, ...
     */
    private String refType;

    // ID tham chiếu (ví dụ: orderItemId)
    private Long refId;

    // Ghi chú
    private String note;

    // Thời điểm tạo record
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
