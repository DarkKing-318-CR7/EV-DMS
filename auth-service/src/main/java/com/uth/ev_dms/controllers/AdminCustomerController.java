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
@RequestMapping({"/dealer/customers", "/staff/customers", "/manager/customers"})
public class AdminCustomerController {

    private final CrmFeignClient crmFeignClient;
    private final CrmAdminMapper mapper;
    private final com.uth.ev_dms.repo.CustomerRepo customerRepo;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       Model model) {
        model.addAttribute("active", "customers");

        List<CustomerVm> customers;
        try {
            List<CustomerFeignDto> dtos = crmFeignClient.listCustomers(q);
            customers = mapper.toCustomerVmList(dtos);
        } catch (Exception ex) {
            List<com.uth.ev_dms.domain.Customer> localList = (q != null && !q.isBlank())
                    ? customerRepo.searchAll(q.trim())
                    : customerRepo.findAll();
            customers = localList.stream().map(c -> {
                CustomerVm vm = new CustomerVm();
                vm.setId(c.getId());
                vm.setTen(c.getTen());
                vm.setSdt(c.getSdt());
                vm.setEmail(c.getEmail());
                vm.setDiachi(c.getDiachi());
                vm.setStatus(c.getStatus() != null ? c.getStatus().name() : "ACTIVE");
                vm.setCreatedAt(c.getCreatedAt());
                return vm;
            }).toList();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("q", q);

        return "dealer/customers";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerVm());
        return "dealer/customers";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("customer") CustomerVm vm) {
        try {
            CustomerFeignDto dto = new CustomerFeignDto();
            dto.setTen(vm.getTen());
            dto.setSdt(vm.getSdt());
            dto.setEmail(vm.getEmail());
            dto.setDiachi(vm.getDiachi());
            crmFeignClient.createCustomer(dto);
        } catch (Exception ex) {
            com.uth.ev_dms.domain.Customer c = new com.uth.ev_dms.domain.Customer();
            c.setTen(vm.getTen());
            c.setSdt(vm.getSdt());
            c.setEmail(vm.getEmail());
            c.setDiachi(vm.getDiachi());
            c.setStatus(com.uth.ev_dms.domain.CustomerStatus.ACTIVE);
            c.setCreatedAt(java.time.Instant.now());
            customerRepo.save(c);
        }
        return "redirect:/dealer/customers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return "redirect:/dealer/customers";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("customer") CustomerVm vm) {
        try {
            CustomerFeignDto dto = new CustomerFeignDto();
            dto.setId(id);
            dto.setTen(vm.getTen());
            dto.setSdt(vm.getSdt());
            dto.setEmail(vm.getEmail());
            dto.setDiachi(vm.getDiachi());
            dto.setStatus(vm.getStatus());
            crmFeignClient.updateCustomer(id, dto);
        } catch (Exception ex) {
            customerRepo.findById(id).ifPresent(c -> {
                c.setTen(vm.getTen());
                c.setSdt(vm.getSdt());
                c.setEmail(vm.getEmail());
                c.setDiachi(vm.getDiachi());
                customerRepo.save(c);
            });
        }
        return "redirect:/dealer/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            crmFeignClient.deleteCustomer(id);
        } catch (Exception ex) {
            customerRepo.deleteById(id);
        }
        return "redirect:/dealer/customers";
    }
}
