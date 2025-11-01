package com.uth.ev_dms.service;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.repo.DealerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepo dealerRepo;

    public List<Dealer> getAllDealers() {
        return dealerRepo.findAll();
    }
}
