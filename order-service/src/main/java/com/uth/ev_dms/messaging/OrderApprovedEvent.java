package com.uth.ev_dms.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderApprovedEvent {
    private Long orderId;
    private Long staffId;
}
