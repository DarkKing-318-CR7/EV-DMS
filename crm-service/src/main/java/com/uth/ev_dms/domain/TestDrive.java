package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_drives")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerPhone;
    private String vehicleName;
    private String location;

    private LocalDateTime scheduleAt;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long vehicleId;
    private Long trimId;

    private String notes;

    @Enumerated(EnumType.STRING)
    private TestDriveStatus status;

    /**
     * ⭐ Chuẩn microservice:
     * Không dùng User entity → chỉ lưu ID
     */
    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "assigned_staff_id")
    private Long assignedStaffId;

    /**
     * ⭐ Chuẩn microservice:
     * Không dùng Dealer entity → chỉ lưu ID
     */
    @Column(name = "dealer_id")
    private Long dealerId;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TestDriveStatus.REQUESTED;
        }
    }
}
