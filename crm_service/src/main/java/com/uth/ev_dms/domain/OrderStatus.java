package com.uth.ev_dms.domain;

public enum OrderStatus {
    NEW,          // vua tao tu quote hoac manual
    PENDING_ALLOC,// cho phe duyet/phan bo
    ALLOCATED,    // da giu/phan xe thanh cong
    DELIVERED,    // da giao xe
    CANCELLED
}
