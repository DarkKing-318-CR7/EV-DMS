package com.uth.ev_dms.service;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.PromotionRepo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {

    private final PromotionRepo promotionRepo;
    private final DealerBranchRepo dealerBranchRepo;

    public PromotionService(PromotionRepo promotionRepo,
                            DealerBranchRepo dealerBranchRepo) {
        this.promotionRepo = promotionRepo;
        this.dealerBranchRepo = dealerBranchRepo;
    }

    // =====================================================
    // ================ READ – LIST ALL ====================
    // =====================================================

    @Cacheable(value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE)
    public List<Promotion> getAllPromotions() {
        return promotionRepo.findAll();
    }

    @Cacheable(
            value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE,
            key = "#id"
    )
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepo.findById(id);
    }

    // =====================================================
    // ================== CREATE / UPDATE ==================
    // =====================================================

    @CacheEvict(
            value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE,
            allEntries = true
    )
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepo.save(promotion);
    }

    @CacheEvict(
            value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE,
            allEntries = true
    )
    public void deletePromotion(Long id) {
        promotionRepo.deleteById(id);
    }

    // =====================================================
    // ================ VALIDATION + FILTER ================
    // =====================================================

    @Cacheable(
            value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE,
            key = "T(java.util.Objects).hash(#dealerId, #trimId, #branchId, #today)"
    )
    public List<Promotion> getValidPromotions(Long dealerId,
                                              Long trimId,
                                              Long branchId,
                                              LocalDate today) {

        int totalBranches = dealerBranchRepo.findAll().size();

        return promotionRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> p.getStartDate() == null || !today.isBefore(p.getStartDate()))
                .filter(p -> p.getEndDate() == null || !today.isAfter(p.getEndDate()))
                .filter(p -> p.getDealerId() == null || p.getDealerId().equals(dealerId))
                .filter(p -> p.getVehicleTrimId() == null || p.getVehicleTrimId().equals(trimId))

                // ===== Branch filter (đã hợp nhất) =====
                .filter(p -> {
                    List<Long> branches = p.getBranchIds();

                    if (branches != null && branches.size() == totalBranches)
                        return true; // ALL

                    if (branches == null || branches.isEmpty())
                        return true; // Không chọn → ALL

                    if (branchId == null)
                        return false;

                    return branches.contains(branchId);
                })

                .toList();
    }


    public boolean validatePromotion(Promotion p,
                                     Long dealerId,
                                     Long trimId,
                                     Long branchId,
                                     LocalDate today) {

        if (!Boolean.TRUE.equals(p.getActive())) return false;

        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;

        if (p.getDealerId() != null && !p.getDealerId().equals(dealerId)) return false;

        if (p.getVehicleTrimId() != null && !p.getVehicleTrimId().equals(trimId)) return false;

        List<Long> branches = p.getBranchIds();

        if (branches != null && !branches.isEmpty()) {
            if (branchId == null) return false;
            if (!branches.contains(branchId)) return false;
        }

        return true;
    }

    // =====================================================
    // ===================== DISCOUNT ======================
    // =====================================================

    public BigDecimal computeDiscount(BigDecimal quoteTotal, List<Long> promotionIds) {
        BigDecimal total = BigDecimal.ZERO;
        if (promotionIds == null) return total;

        for (Long id : promotionIds) {
            Promotion p = promotionRepo.findById(id).orElse(null);
            if (p == null || p.getDiscountPercent() == null) continue;

            BigDecimal d = quoteTotal.multiply(p.getDiscountPercent())
                    .divide(new BigDecimal("100"));

            total = total.add(d);
        }
        return total.max(BigDecimal.ZERO);
    }

    @Cacheable(value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE)
    public List<Promotion> findAll() {
        return promotionRepo.findAll();
    }

    @Cacheable(
            value = CacheConfig.CacheNames.PROMOTIONS_ACTIVE,
            key = "'quote_' + #dealerId + '_' + #trimId + '_' + #branchId"
    )
    public List<Promotion> getValidPromotionsForQuote(Long dealerId,
                                                      Long trimId,
                                                      Long branchId) {

        return getValidPromotions(dealerId, trimId, branchId, LocalDate.now());
    }

    public BigDecimal computeDiscountForQuote(BigDecimal quoteTotal,
                                              List<Long> promotionIds,
                                              Long dealerId,
                                              Long trimId,
                                              Long branchId,
                                              LocalDate today) {

        BigDecimal total = BigDecimal.ZERO;

        if (promotionIds == null) return total;

        for (Long id : promotionIds) {
            Promotion p = promotionRepo.findById(id).orElse(null);
            if (p == null) continue;

            if (!validatePromotion(p, dealerId, trimId, branchId, today)) continue;
            if (p.getDiscountPercent() == null) continue;

            BigDecimal d = quoteTotal.multiply(p.getDiscountPercent())
                    .divide(new BigDecimal("100"));

            total = total.add(d);
        }

        if (total.compareTo(quoteTotal) > 0) {
            total = quoteTotal;
        }

        return total.max(BigDecimal.ZERO);
    }
}
