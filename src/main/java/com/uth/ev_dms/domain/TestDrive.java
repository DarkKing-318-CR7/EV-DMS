package com.uth.ev_dms.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity(name = "CrmTestDrive") // dat entity name khac de khong trung voi CRM
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "crm_test_drive")       // dung underscore, tranh dau '-'
public class TestDrive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long vehicleId;
    private Long staffId;

    private LocalDateTime scheduleTime;
    private String note;
    private String status; // PENDING, APPROVED, DONE, CANCELLED
}
