package com.uth.ev_dms.service.vm;

import com.uth.ev_dms.domain.InstallmentPlan;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderItem;
import com.uth.ev_dms.domain.Payment;

import java.util.List;

public class OrderDetailVM {
    private OrderHdr order;
    private List<OrderItem> items;
    private List<Payment> payments;
    private InstallmentPlan installment;

    public OrderHdr getOrder() { return order; }
    public void setOrder(OrderHdr order) { this.order = order; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public InstallmentPlan getInstallment() { return installment; }
    public void setInstallment(InstallmentPlan installment) { this.installment = installment; }
}
