package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.DealerBranch;
import com.uth.ev_dms.repo.DealerBranchRepo;
import com.uth.ev_dms.repo.DealerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
public class DealerService {
    private final DealerRepo dealerRepo;
    private final DealerBranchRepo branchRepo;

    public List<Dealer> getAllDealers() {
        return dealerRepo.findAll();
    }

    public List<Dealer> list() { return dealerRepo.findAll(); }

    public Dealer get(Long id) {
        return dealerRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Dealer not found"));
    }

    @Transactional
    public Dealer create(Dealer input) {
        if (dealerRepo.existsByCode(input.getCode())) {
            throw new IllegalArgumentException("Dealer code already exists");
        }
        Dealer saved = dealerRepo.save(input);

        DealerBranch main = DealerBranch.builder()
                .dealer(saved)
                .code("MAIN")
                .name(saved.getName() + " - Main")
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .addressLine1(saved.getAddressLine1())
                .addressLine2(saved.getAddressLine2())
                .ward(saved.getWard())
                .district(saved.getDistrict())
                .province(saved.getProvince())
                .status(DealerBranch.Status.ACTIVE)
                .build();
        branchRepo.save(main);
        return saved;
    }

    @Transactional
    public Dealer update(Long id, Dealer input) {
        Dealer d = get(id);
        if (!d.getCode().equals(input.getCode()) && dealerRepo.existsByCode(input.getCode())) {
            throw new IllegalArgumentException("Dealer code already exists");
        }
        d.setCode(input.getCode());
        d.setName(input.getName());
        d.setRegion(input.getRegion());
        d.setPhone(input.getPhone());
        d.setEmail(input.getEmail());
        d.setAddressLine1(input.getAddressLine1());
        d.setAddressLine2(input.getAddressLine2());
        d.setWard(input.getWard());
        d.setDistrict(input.getDistrict());
        d.setProvince(input.getProvince());
        d.setStatus(input.getStatus());
        // đồng bộ nhẹ sang MAIN branch
        branchRepo.findByDealerId(d.getId()).ifPresent(b -> {
            b.setName(d.getName() + " - Main");
            b.setPhone(d.getPhone());
            b.setEmail(d.getEmail());
            b.setAddressLine1(d.getAddressLine1());
            b.setAddressLine2(d.getAddressLine2());
            b.setWard(d.getWard());
            b.setDistrict(d.getDistrict());
            b.setProvince(d.getProvince());
        });
        return d;
    }

    @Transactional
    public void delete(Long id) {
        // Nếu đã liên kết inventories/orders… bạn có thể chuyển sang status INACTIVE thay vì xóa cứng
        branchRepo.findByDealerId(id).ifPresent(branchRepo::delete);
        dealerRepo.deleteById(id);
    }
}
