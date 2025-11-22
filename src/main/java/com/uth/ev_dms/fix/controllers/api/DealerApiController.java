package com.uth.ev_dms.fix.controllers.api;

import com.uth.ev_dms.fix.service.dto.DealerBranchDto;
import com.uth.ev_dms.fix.service.dto.DealerDto;
import com.uth.ev_dms.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
@RequiredArgsConstructor
public class DealerApiController {

    private final DealerService dealerService;

    // LẤY TẤT CẢ DEALER – Inventory service sẽ dùng hàm này
    @GetMapping
    public List<DealerDto> getAllDealers() {
        return dealerService.getAllDealersDto();
    }

    // LẤY 1 DEALER THEO ID
    @GetMapping("/{id}")
    public DealerDto getDealer(@PathVariable Long id) {
        return dealerService.getDealerDto(id);
    }

    // LẤY CÁC CHI NHÁNH CỦA 1 DEALER
    @GetMapping("/{id}/branches")
    public List<DealerBranchDto> getBranches(@PathVariable Long id) {
        return dealerService.getDealerBranchesDto(id);
    }
}
