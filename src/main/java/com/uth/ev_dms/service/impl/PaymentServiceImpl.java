package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.OrderStatus;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.domain.PaymentType;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.PaymentRepo;
import com.uth.ev_dms.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentRepo.findByOrder_IdOrderByPaidAtDesc(orderId);
    }

    @Override
    public boolean hasInstallment(Long orderId) {
        return paymentRepo.existsByOrder_IdAndType(orderId, PaymentType.INSTALLMENT);
    }

    @Override
    @Transactional
    public Payment addPayment(Long orderId,
                              BigDecimal amount,
                              String method,
                              String refNo,
                              String note,
                              PaymentType type) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("INVALID_AMOUNT");
        }

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        // không thu tiền khi CANCELLED / DELIVERED
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("ORDER_STATUS_NOT_ALLOWED_FOR_PAYMENT");
        }

        BigDecimal total  = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal paid   = order.getPaidAmount()  == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal remain = total.subtract(paid);

        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("ORDER_ALREADY_FULLY_PAID");
        }
        if (amount.compareTo(remain) > 0) {
            throw new IllegalStateException("PAYMENT_EXCEEDS_REMAIN");
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(amount);
        p.setMethod((method == null || method.isBlank()) ? "CASH" : method);
        p.setRefNo(refNo);
        p.setNote(note);
        p.setType(type == null ? PaymentType.CASH : type);
        p.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepo.save(p);

        // cập nhật paid / balance
        BigDecimal paidNew = paid.add(amount);
        order.setPaidAmount(paidNew);
        order.setBalanceAmount(total.subtract(paidNew));
        orderRepo.save(order);

        return saved;
    }

    @Override
    @Transactional
    public Payment addPayment(Long orderId,
                              BigDecimal amount,
                              String method,
                              String refNo) {
        String m = (method == null || method.isBlank()) ? "CASH" : method;
        return addPayment(orderId, amount, m, refNo, null, PaymentType.CASH);
    }

    @Override
    @Transactional
    public Payment createInstallment(Long orderId, int tenorMonths, BigDecimal downPayment) {
        if (tenorMonths <= 0) throw new IllegalArgumentException("INVALID_TENOR");
        if (downPayment == null || downPayment.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("INVALID_DOWN_PAYMENT");

        OrderHdr order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_NOT_FOUND"));

        // chỉ NEW hoặc PENDING_ALLOC; cấm CANCELLED/DELIVERED
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("ORDER_STATUS_NOT_ALLOWED_FOR_INSTALLMENT");
        }
        if (!(order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PENDING_ALLOC)) {
            throw new IllegalStateException("ORDER_STATUS_BLOCKED_FOR_INSTALLMENT");
        }

        // một đơn chỉ có 1 kế hoạch trả góp
        if (hasInstallment(orderId)) {
            throw new IllegalStateException("INSTALLMENT_ALREADY_EXISTS");
        }

        BigDecimal total  = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal paid   = order.getPaidAmount()  == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal remain = total.subtract(paid);

        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("ORDER_ALREADY_FULLY_PAID");
        }
        if (downPayment.compareTo(remain) > 0) {
            throw new IllegalStateException("DOWNPAYMENT_EXCEEDS_REMAIN");
        }

        Payment p = new Payment();
        p.setOrder(order);
        p.setAmount(downPayment);
        p.setMethod("INSTALLMENT_PLAN");
        p.setRefNo("INSTALL-" + orderId + "-" + System.currentTimeMillis());
        p.setNote("Ke hoach tra gop " + tenorMonths + " thang");
        p.setType(PaymentType.INSTALLMENT);
        p.setPaidAt(LocalDateTime.now());
        paymentRepo.save(p);

        // cập nhật paid / balance
        BigDecimal paidNew = paid.add(downPayment);
        order.setPaidAmount(paidNew);
        order.setBalanceAmount(total.subtract(paidNew));
        orderRepo.save(order);

        return p;
    }
}
