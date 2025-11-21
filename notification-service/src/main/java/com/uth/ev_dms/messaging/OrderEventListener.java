package com.uth.ev_dms.messaging;

import com.uth.ev_dms.config.RabbitConfig;
import com.uth.ev_dms.service.FirebaseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final FirebaseNotificationService firebaseNotificationService;

    @RabbitListener(queues = RabbitConfig.QUEUE_ORDER_APPROVED)
    public void onOrderApproved(OrderApprovedEvent event) {

        System.out.println("📥 Received MQ ORDER_APPROVED: orderId="
                + event.getOrderId() + ", userId=" + event.getUserId());

        firebaseNotificationService.sendOrderApproved(
                event.getOrderId(),
                event.getUserId()
        );
    }

}
