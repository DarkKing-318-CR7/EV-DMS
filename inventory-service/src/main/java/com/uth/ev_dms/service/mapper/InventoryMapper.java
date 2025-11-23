package com.uth.ev_dms.service.mapper;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.DealerBranch;
import com.uth.ev_dms.domain.Inventory;
import com.uth.ev_dms.domain.Trim;
import com.uth.ev_dms.service.dto.InventoryDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Component
public class InventoryMapper {

    public InventoryDto toDto(Inventory inv) {
        if (inv == null) {
            return null;
        }

        InventoryDto dto = new InventoryDto();
        dto.setId(inv.getId());

        if (inv.getDealer() != null) {
            dto.setDealerId(inv.getDealer().getId());
        }
        if (inv.getBranch() != null) {
            dto.setBranchId(inv.getBranch().getId());
        }
        if (inv.getTrim() != null) {
            dto.setTrimId(inv.getTrim().getId());
            dto.setTrimName(inv.getTrim().getTrimName());
//            dto.setModelName(inv.getTrim().getModelName());
        }

        int onHand = inv.getQtyOnHand() == null ? 0 : inv.getQtyOnHand();
        int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
        dto.setQtyOnHand(onHand);
        dto.setReserved(reserved);
        dto.setAvailable(onHand - reserved);


        if (inv.getUpdatedAt() != null) {
            dto.setUpdatedAt(LocalDateTime.ofInstant(
                    inv.getUpdatedAt(),
                    ZoneId.systemDefault()
            ));
        }

        return dto;
    }

    public Inventory toEntity(InventoryDto dto) {
        if (dto == null) return null;

        Inventory inv = new Inventory();
        inv.setId(dto.getId());

        // Dealer
        if (dto.getDealerId() != null) {
            Dealer d = new Dealer();
            d.setId(dto.getDealerId());
            inv.setDealer(d);
        }

        // Branch
        if (dto.getBranchId() != null) {
            DealerBranch b = new DealerBranch();
            b.setId(dto.getBranchId());
            inv.setBranch(b);
        }

        // Trim
        if (dto.getTrimId() != null) {
            Trim t = new Trim();
            t.setId(dto.getTrimId());
            inv.setTrim(t);
        }

        inv.setQtyOnHand(dto.getQtyOnHand());
        inv.setReserved(dto.getReserved());
//        inv.setColor(dto.getColor());
//        inv.setVin(dto.getVin());

        return inv;
    }

    private Integer defaultZero(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }
}
