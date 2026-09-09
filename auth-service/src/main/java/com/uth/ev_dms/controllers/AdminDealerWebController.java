package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.repo.DealerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/dealers")
@RequiredArgsConstructor
public class AdminDealerWebController {

    private final DealerRepo dealerRepo;

    @GetMapping
    public String list(Model model) {
        List<Dealer> dealers = dealerRepo.findAll();
        model.addAttribute("dealers", dealers);
        model.addAttribute("active", "dealers");
        model.addAttribute("pageTitle", "Quản Lý Đại Lý Phân Phối");
        return "admin/dealers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dealer", new Dealer());
        model.addAttribute("formTitle", "Thêm Đại Lý Mới");
        model.addAttribute("formAction", "/admin/dealers/create");
        model.addAttribute("active", "dealers");
        model.addAttribute("pageTitle", "Thêm Mới Đại Lý");
        return "admin/dealers/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Dealer dealer, RedirectAttributes ra) {
        dealerRepo.save(dealer);
        ra.addFlashAttribute("ok", "Đã thêm đại lý mới thành công: " + dealer.getName());
        return "redirect:/admin/dealers";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        Dealer dealer = dealerRepo.findById(id).orElse(null);
        if (dealer == null) {
            return "redirect:/admin/dealers";
        }
        model.addAttribute("dealer", dealer);
        model.addAttribute("active", "dealers");
        model.addAttribute("pageTitle", "Chi Tiết Đại Lý - " + dealer.getName());
        return "admin/dealers/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable(name = "id") Long id, Model model) {
        Dealer dealer = dealerRepo.findById(id).orElse(null);
        if (dealer == null) {
            return "redirect:/admin/dealers";
        }
        model.addAttribute("dealer", dealer);
        model.addAttribute("formTitle", "Chỉnh Sửa Đại Lý: " + dealer.getName());
        model.addAttribute("formAction", "/admin/dealers/" + id + "/edit");
        model.addAttribute("active", "dealers");
        model.addAttribute("pageTitle", "Chỉnh Sửa Đại Lý");
        return "admin/dealers/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable(name = "id") Long id, @ModelAttribute Dealer form, RedirectAttributes ra) {
        Dealer d = dealerRepo.findById(id).orElse(null);
        if (d != null) {
            d.setCode(form.getCode());
            d.setName(form.getName());
            d.setRegion(form.getRegion());
            d.setPhone(form.getPhone());
            d.setEmail(form.getEmail());
            d.setAddressLine1(form.getAddressLine1());
            d.setAddressLine2(form.getAddressLine2());
            d.setWard(form.getWard());
            d.setDistrict(form.getDistrict());
            d.setProvince(form.getProvince());
            d.setStatus(form.getStatus());
            dealerRepo.save(d);
            ra.addFlashAttribute("ok", "Đã cập nhật đại lý thành công: " + d.getName());
        }
        return "redirect:/admin/dealers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable(name = "id") Long id, RedirectAttributes ra) {
        dealerRepo.findById(id).ifPresent(d -> {
            dealerRepo.delete(d);
            ra.addFlashAttribute("ok", "Đã xóa đại lý: " + d.getName());
        });
        return "redirect:/admin/dealers";
    }
}
