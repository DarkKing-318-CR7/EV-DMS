package com.uth.ev_dms.domain;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    public java.time.LocalDateTime getCreatedAtLdt() {
        return createdAt == null ? null
                : createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }
    public java.time.LocalDateTime getUpdatedAtLdt() {
        return updatedAt == null ? null
                : updatedAt.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }
}
