package com.uth.ev_dms.controllers;

import com.uth.ev_dms.client.CrmFeignClient;
import com.uth.ev_dms.client.dto.CustomerFeignDto;
import com.uth.ev_dms.web.mapper.CrmAdminMapper;
import com.uth.ev_dms.web.vm.CustomerVm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/customers")
public class AdminCustomerController {

    private final CrmFeignClient crmFeignClient;
    private final CrmAdminMapper mapper;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       Model model) {

        List<CustomerFeignDto> dtos = crmFeignClient.listCustomers(q);
        List<CustomerVm> customers = mapper.toCustomerVmList(dtos);

        model.addAttribute("customers", customers);
        model.addAttribute("q", q);

        return "dealer/customers";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerVm());
        return "admin/crm/customers/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("customer") CustomerVm vm) {
        CustomerFeignDto dto = new CustomerFeignDto();
        dto.setTen(vm.getTen());
        dto.setSdt(vm.getSdt());
        dto.setEmail(vm.getEmail());
        dto.setDiachi(vm.getDiachi());

        crmFeignClient.createCustomer(dto);
        return "redirect:/admin/crm/customers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        // tạm thời gọi list rồi filter, nếu muốn tối ưu: thêm API getById bên CRM
        List<CustomerFeignDto> all = crmFeignClient.listCustomers(null);
        CustomerFeignDto dto = all.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow();

        CustomerVm vm = mapper.toCustomerVm(dto);
        model.addAttribute("customer", vm);
        return "admin/crm/customers/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("customer") CustomerVm vm) {

        CustomerFeignDto dto = new CustomerFeignDto();
        dto.setId(id);
        dto.setTen(vm.getTen());
        dto.setSdt(vm.getSdt());
        dto.setEmail(vm.getEmail());
        dto.setDiachi(vm.getDiachi());
        dto.setStatus(vm.getStatus());

        crmFeignClient.updateCustomer(id, dto);
        return "redirect:/admin/crm/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        crmFeignClient.deleteCustomer(id);
        return "redirect:/admin/crm/customers";
    }
}
