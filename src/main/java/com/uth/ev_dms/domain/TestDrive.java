package com.uth.ev_dms.domain;

import com.uth.ev_dms.auth.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="test_drives")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TestDrive {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerPhone;
    private String vehicleName;            // đơn giản: tên xe
    private String location;

    private LocalDateTime scheduleAt;      // thời gian hẹn
    private String notes;

    @Enumerated(EnumType.STRING)
    private TestDriveStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;                 // staff tạo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;            // staff phụ trách (optional)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;                 // đại lý thuộc manager
}
