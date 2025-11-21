package com.uth.ev_dms.service;

import com.uth.ev_dms.config.CacheConfig;
import com.uth.ev_dms.domain.Promotion;
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

    public PromotionService(PromotionRepo promotionRepo) {
        this.promotionRepo = promotionRepo;
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
            value = {
                    CacheConfig.CacheNames.PROMOTIONS_ACTIVE
            },
            allEntries = true
    )
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepo.save(promotion);
    }

    @CacheEvict(
            value = {
                    CacheConfig.CacheNames.PROMOTIONS_ACTIVE
            },
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
            key = "T(java.util.Objects).hash(#dealerId, #trimId, #region, #today)"
    )
    public List<Promotion> getValidPromotions(Long dealerId, Long trimId, String region, LocalDate today) {
        return promotionRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> p.getStartDate() == null || !today.isBefore(p.getStartDate()))
                .filter(p -> p.getEndDate() == null || !today.isAfter(p.getEndDate()))
                .filter(p -> p.getDealerId() == null || p.getDealerId().equals(dealerId))
                .filter(p -> {
                    List<String> regions = p.getRegions();
                    if (regions == null || regions.isEmpty()) return true;
                    if (regions.contains("ALL")) return true;
                    if (region == null) return false;
                    return regions.contains(region);
                })
                .filter(p -> p.getVehicleTrimId() == null || p.getVehicleTrimId().equals(trimId))
                .toList();
    }

    public boolean validatePromotion(Promotion p, Long dealerId, Long trimId, String region, LocalDate today) {
        if (!Boolean.TRUE.equals(p.getActive())) return false;
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;
        if (p.getDealerId() != null && !p.getDealerId().equals(dealerId)) return false;

        List<String> promoRegions = p.getRegions();
        if (promoRegions != null && !promoRegions.isEmpty() && !promoRegions.contains("ALL")) {
            if (region == null || !promoRegions.contains(region)) return false;
        }

        if (p.getVehicleTrimId() != null && !p.getVehicleTrimId().equals(trimId)) return false;

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
            key = "'quote_' + #dealerId + '_' + #trimId + '_' + #region"
    )
    public List<Promotion> getValidPromotionsForQuote(Long dealerId, Long trimId, String region) {
        return getValidPromotions(dealerId, trimId, region, LocalDate.now());
    }

    public BigDecimal computeDiscountForQuote(BigDecimal quoteTotal,
                                              List<Long> promotionIds,
                                              Long dealerId,
                                              Long trimId,
                                              String region,
                                              LocalDate today) {

        BigDecimal total = BigDecimal.ZERO;

        if (promotionIds == null) return total;

        for (Long id : promotionIds) {
            Promotion p = promotionRepo.findById(id).orElse(null);
            if (p == null) continue;

            if (!validatePromotion(p, dealerId, trimId, region, today)) continue;
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
