package com.uth.ev_dms.mapper;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.dto.CustomerDto;
import com.uth.ev_dms.dto.TestDriveDto;
import org.springframework.stereotype.Component;

@Component
public class CrmMapper {

    // =======================
    //   CUSTOMER → DTO
    // =======================
    public CustomerDto toCustomerDto(Customer c) {
        if (c == null) return null;

        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setTen(c.getTen());
        dto.setSdt(c.getSdt());
        dto.setEmail(c.getEmail());
        dto.setDiachi(c.getDiachi());

        // status: ENUM -> String (CustomerStatus or TestDriveStatus tùy bạn đặt)
        if (c.getStatus() != null) {
            dto.setStatus(c.getStatus().name());
        }

        dto.setCreatedAt(c.getCreatedAt());
        dto.setNgaytao(c.getNgaytao());

        return dto;
    }

    // =======================
    //   TEST DRIVE → DTO
    // =======================
    public TestDriveDto toTestDriveDto(TestDrive t) {
        if (t == null) return null;

        TestDriveDto dto = new TestDriveDto();
        dto.setId(t.getId());

        // TẠM THỜI KHÔNG DÙNG các getter không tồn tại trong TestDrive
        // (customer, vehicle, trim, location, scheduleTime, note, dealerId)
        // Khi entity ổn định, mình sẽ map lại chi tiết.

        if (t.getStatus() != null) {
            dto.setStatus(t.getStatus().name());
        }

        return dto;
    }
}
