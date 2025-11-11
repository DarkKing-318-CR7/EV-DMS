package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.PromotionRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    private final PromotionRepo promotionRepo;

    public PromotionService(PromotionRepo promotionRepo) {
        this.promotionRepo = promotionRepo;
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepo.findAll();
    }

    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepo.findById(id);
    }

    public Promotion savePromotion(Promotion promotion) {
        return promotionRepo.save(promotion);
    }

    public void deletePromotion(Long id) {
        promotionRepo.deleteById(id);
    }


    // ===== validate/apply =====
    public List<Promotion> getValidPromotions(Long dealerId, Long trimId, String region, LocalDate today) {
        return promotionRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> p.getStartDate() == null || !today.isBefore(p.getStartDate()))
                .filter(p -> p.getEndDate() == null || !today.isAfter(p.getEndDate()))
                .filter(p -> p.getDealerId() == null || p.getDealerId().equals(dealerId))
                .filter(p -> p.getRegion() == null || p.getRegion().equalsIgnoreCase(region))
                .filter(p -> p.getVehicleTrimId() == null || p.getVehicleTrimId().equals(trimId))
                .toList();
    }



    public boolean validatePromotion(Promotion p, Long dealerId, Long trimId, String region, LocalDate today) {
        if (!Boolean.TRUE.equals(p.getActive())) return false;
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;
        if (p.getDealerId() != null && !p.getDealerId().equals(dealerId)) return false;
        if (p.getRegion() != null && !p.getRegion().equalsIgnoreCase(region)) return false;
        if (p.getVehicleTrimId() != null && !p.getVehicleTrimId().equals(trimId)) return false;
        // (Optional) ngân sách: nếu cần, kiểm tra budget > 0
        return true;
    }


    // Tính tổng giảm dựa trên phần trăm, policy chống "xung đột" do bạn định nghĩa
    public BigDecimal computeDiscount(BigDecimal quoteTotal, List<Long> promotionIds) {
        BigDecimal total = BigDecimal.ZERO;
        if (promotionIds == null) return total;

        for (Long id : promotionIds) {
            Optional<Promotion> opt = promotionRepo.findById(id);
            if (opt.isEmpty()) continue;
            Promotion p = opt.get();
            if (p.getDiscountPercent() == null) continue;

            BigDecimal d = quoteTotal.multiply(p.getDiscountPercent())
                    .divide(new BigDecimal("100"));
            total = total.add(d);
        }
        return total.max(BigDecimal.ZERO);
    }

    // ✅ Trả về toàn bộ danh sách khuyến mãi (cho Manager xem)
    public List<Promotion> findAll() {
        return promotionRepo.findAll();
    }

}
