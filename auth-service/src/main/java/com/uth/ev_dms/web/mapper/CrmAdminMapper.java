package com.uth.ev_dms.web.mapper;

import com.uth.ev_dms.client.dto.CustomerFeignDto;
import com.uth.ev_dms.client.dto.TestDriveFeignDto;
import com.uth.ev_dms.web.vm.CustomerVm;
import com.uth.ev_dms.web.vm.TestDriveVm;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CrmAdminMapper {

    public CustomerVm toCustomerVm(CustomerFeignDto dto) {
        if (dto == null) return null;
        CustomerVm vm = new CustomerVm();

        vm.setId(dto.getId());
        vm.setTen(dto.getTen());
        vm.setSdt(dto.getSdt());
        vm.setEmail(dto.getEmail());
        vm.setDiachi(dto.getDiachi());
        vm.setStatus(dto.getStatus());
        vm.setCreatedAt(dto.getCreatedAt());
        vm.setNgaytao(dto.getNgaytao());

        return vm;
    }

    public List<CustomerVm> toCustomerVmList(List<CustomerFeignDto> list) {
        return list == null ? List.of()
                : list.stream().map(this::toCustomerVm).collect(Collectors.toList());
    }

    public TestDriveVm toTestDriveVm(TestDriveFeignDto dto) {
        if (dto == null) return null;
        TestDriveVm vm = new TestDriveVm();

        vm.setId(dto.getId());
        vm.setStatus(dto.getStatus());
        vm.setDealerId(dto.getDealerId());

        return vm;
    }

    public List<TestDriveVm> toTestDriveVmList(List<TestDriveFeignDto> list) {
        return list == null ? List.of()
                : list.stream().map(this::toTestDriveVm).collect(Collectors.toList());
    }
}
