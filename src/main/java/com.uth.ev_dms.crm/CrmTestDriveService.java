package com.uth.ev_dms.crm;

import com.uth.ev_dms.crm.dto.TestDriveCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmTestDriveService {

    private final CrmTestDriveRepo repo;
    private final CustomerRepo customerRepo; // dùng để lấy Customer theo id

    /* ====== LIST (khớp CrmApi gọi) ====== */
    public List<TestDrive> list(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return repo.findByNgayBetween(from, to);
        }
        return repo.findAll();
    }

    /* ====== FIND ====== */
    public List<TestDrive> findAll() {
        return repo.findAll();
    }

    public TestDrive findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    /* ====== CREATE (DTO) – khớp CrmApi gọi ====== */
    @Transactional
    public TestDrive create(TestDriveCreateDto dto) {
        var customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + dto.getCustomerId()));

        TestDrive td = new TestDrive();
        td.setCustomer(customer);
        td.setNgay(dto.getNgay());
        td.setGio(dto.getGio());
        td.setXe(dto.getXe());
        td.setGhichu(dto.getGhichu());
        td.setTrangthai(TestDriveStatus.PENDING);

        return repo.save(td);
    }

    /* (Giữ lại overload nếu ở nơi khác đang dùng) */
    @Transactional
    public TestDrive create(TestDrive td) {
        if (td.getTrangthai() == null) td.setTrangthai(TestDriveStatus.PENDING);
        return repo.save(td);
    }

    /* ====== UPDATE STATUS – khớp CrmApi gọi ====== */
    @Transactional
    public TestDrive updateStatus(Long id, TestDriveStatus status) {
        TestDrive td = repo.findById(id).orElseThrow();
        td.setTrangthai(status);
        return repo.save(td);
    }

    /* (Giữ lại alias nếu bạn đã dùng tên này ở chỗ khác) */
    @Transactional
    public TestDrive changeStatus(Long id, TestDriveStatus status) {
        return updateStatus(id, status);
    }

    /* ====== DELETE ====== */
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
