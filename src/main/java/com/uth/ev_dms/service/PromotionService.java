package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Promotion;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.PromotionRepo;
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

    // ========================================
    // VALIDATION + FILTER
    // ========================================
    public List<Promotion> getValidPromotions(Long dealerId, Long trimId, Long branchId, LocalDate today) {
        return promotionRepo.findAll().stream()
                // 1. Đang active
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                // 2. Đang trong khoảng ngày
                .filter(p -> p.getStartDate() == null || !today.isBefore(p.getStartDate()))
                .filter(p -> p.getEndDate() == null || !today.isAfter(p.getEndDate()))
                // 3. Dealer khớp (hoặc không set dealerId = áp dụng mọi dealer)
                .filter(p -> p.getDealerId() == null || p.getDealerId().equals(dealerId))
                // 4. Trim khớp (hoặc không set trim = áp dụng mọi trim)
                .filter(p -> p.getVehicleTrimId() == null || p.getVehicleTrimId().equals(trimId))
                // 5. Lọc theo chi nhánh
                .filter(p -> {
                    List<Long> branches = p.getBranchIds();

                    int totalBranches = dealerBranchRepo.findAll().size();

                    // CASE 1: promotion áp dụng cho TẤT CẢ chi nhánh (đủ số lượng)
                    if (branches != null && branches.size() == totalBranches)
                        return true;

                    // CASE 2: promotion không chọn chi nhánh => xem như ALL
                    if (branches == null || branches.isEmpty())
                        return true;

                    // CASE 3: user không có branch => không match
                    if (branchId == null)
                        return false;

                    // CASE 4: lọc theo chi nhánh cụ thể
                    return branches.contains(branchId);
                })


                .toList();
    }


    public boolean validatePromotion(Promotion p,
                                     Long dealerId,
                                     Long trimId,
                                     Long branchId,
                                     LocalDate today) {

        // 1. Active
        if (!Boolean.TRUE.equals(p.getActive())) return false;

        // 2. Ngày hiệu lực
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;

        // 3. Dealer
        if (p.getDealerId() != null && !p.getDealerId().equals(dealerId)) return false;

        // 4. Trim
        if (p.getVehicleTrimId() != null && !p.getVehicleTrimId().equals(trimId)) return false;

        // 5. Chi nhánh
        List<Long> branches = p.getBranchIds();
        if (branches != null && !branches.isEmpty()) {
            // nếu promotion có set branch → bắt buộc user cũng phải có branch và nằm trong list
            if (branchId == null) return false;
            if (!branches.contains(branchId)) return false;
        }
        // nếu branches null / rỗng → promotion áp dụng cho mọi chi nhánh
        return true;
    }


    // Tính tổng giảm
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

    public List<Promotion> findAll() {
        return promotionRepo.findAll();
    }

    public List<Promotion> getValidPromotionsForQuote(Long dealerId, Long trimId, Long branchId) {

        return getValidPromotions(dealerId, trimId, branchId, LocalDate.now());
    }


    public BigDecimal computeDiscountForQuote(BigDecimal quoteTotal,
                                              List<Long> promotionIds,
                                              Long dealerId,
                                              Long trimId,
                                              Long branchId,
                                              LocalDate today)
    {

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
