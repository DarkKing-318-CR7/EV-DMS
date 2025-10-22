package com.uth.ev_dms.service.impl;

import com.uth.ev_dms.domain.InstallmentPlan;
import com.uth.ev_dms.domain.OrderHdr;
import com.uth.ev_dms.domain.Payment;
import com.uth.ev_dms.exception.BusinessException;
import com.uth.ev_dms.repo.InstallmentPlanRepo;
import com.uth.ev_dms.repo.OrderRepo;
import com.uth.ev_dms.repo.PaymentRepo;
import com.uth.ev_dms.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepo orderRepo;
    private final PaymentRepo paymentRepo;
    private final InstallmentPlanRepo planRepo;


    @Override
    public List<Payment> findByOrderId(Long orderId) {
        return paymentRepo.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public Payment addPayment(Long orderId, BigDecimal amount, String method, String note) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be > 0");
        }
        OrderHdr o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        Payment p = new Payment();
        p.setOrder(o);
        p.setAmount(amount);
        p.setMethod(method);
        p.setNote(note);
        paymentRepo.save(p);

        BigDecimal totalPaid = paymentRepo.sumPaid(orderId);
        o.setPaidAmount(totalPaid);
        BigDecimal newBalance = o.getTotalAmount().subtract(totalPaid);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("OVERPAY", "Payment exceeds total");
        }
        o.setBalanceAmount(newBalance);
        orderRepo.save(o);
        return p;
    }

    @Override
    public BigDecimal computeMonthly(BigDecimal principal, int months, BigDecimal annualRatePct) {
        if (months <= 0) return BigDecimal.ZERO;
        BigDecimal monthlyRate = annualRatePct.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP); // /100/12
        BigDecimal onePlusRPowerN = monthlyRate.add(BigDecimal.ONE).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public InstallmentPlan createInstallment(Long orderId, int months, BigDecimal annualRate) {
        OrderHdr o = orderRepo.findById(orderId).orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        BigDecimal monthly = computeMonthly(o.getBalanceAmount(), months, annualRate);
        InstallmentPlan plan = new InstallmentPlan();
        plan.setOrder(o);
        plan.setMonths(months);
        plan.setInterestRate(annualRate);
        plan.setMonthlyPayment(monthly);
        plan.setTotalPayable(monthly.multiply(BigDecimal.valueOf(months)));
        return planRepo.save(plan);
    }
}
