package com.uth.ev_dms.notification;

public class NotificationTemplate {

    public static String orderApprovedTitle(Long orderId) {
        return "Đơn hàng #" + orderId + " đã được duyệt";
    }

    public static String orderApprovedBody() {
        return "Quản lý đã phê duyệt đơn hàng. Vui lòng kiểm tra chi tiết.";
    }
}
