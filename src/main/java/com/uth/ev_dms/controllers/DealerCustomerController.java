package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.Customer;
import com.uth.ev_dms.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/customers")
public class DealerCustomerController {

    private final CustomerService customerService;

    @GetMapping("/my")
    public String myList(@RequestParam(required = false) String q,
                         Authentication auth,
                         Model model) {
        Long userId = currentUserId(auth);
        var list = (q != null && !q.isBlank())
                ? customerService.searchMine(userId, q)
                : customerService.findMine(userId);
        model.addAttribute("list", list);
        model.addAttribute("q", q);
        return "dealer/customers";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("c", new Customer());
        return "dealer/form"; // nếu bạn muốn form riêng: đổi thành dealer/customers-form
    }

    @PostMapping
    public String create(@ModelAttribute("c") Customer c,
                         Authentication auth,
                         RedirectAttributes ra) {
        c.setOwnerId(currentUserId(auth));
        customerService.create(c);
        ra.addFlashAttribute("msg", "Da tao khach hang");
        return "redirect:/dealer/customers";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Authentication auth,
                         Model model,
                         RedirectAttributes ra) {
        Customer c = customerService.findById(id);
        if (c == null) {
            ra.addFlashAttribute("msg", "Khong tim thay");
            return "redirect:/dealer/customers";
        }
        if (!isManager(auth) && (c.getOwnerId() == null || !c.getOwnerId().equals(currentUserId(auth)))) {
            ra.addFlashAttribute("msg", "Khong du quyen");
            return "redirect:/dealer/customers";
        }
        model.addAttribute("c", c);
        return "dealer/detail"; // nếu có trang detail riêng cho customer thì đổi tên file theo bạn
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("c") Customer c,
                         Authentication auth,
                         RedirectAttributes ra) {
        Customer old = customerService.findById(id);
        if (old == null) {
            ra.addFlashAttribute("msg", "Khong tim thay");
            return "redirect:/dealer/customers";
        }
        if (!isManager(auth) && (old.getOwnerId() == null || !old.getOwnerId().equals(currentUserId(auth)))) {
            ra.addFlashAttribute("msg", "Khong du quyen");
            return "redirect:/dealer/customers";
        }
        c.setId(id);
        c.setOwnerId(old.getOwnerId());
        customerService.update(c);
        ra.addFlashAttribute("msg", "Da cap nhat");
        return "redirect:/dealer/customers/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication auth,
                         RedirectAttributes ra) {
        Customer old = customerService.findById(id);
        if (old != null && (isManager(auth) || (old.getOwnerId() != null && old.getOwnerId().equals(currentUserId(auth))))) {
            customerService.delete(id);
            ra.addFlashAttribute("msg", "Da xoa");
        } else {
            ra.addFlashAttribute("msg", "Khong du quyen");
        }
        return "redirect:/dealer/customers";
    }

    private Long currentUserId(Authentication auth) {
        try { return Long.parseLong(auth.getName()); }
        catch (Exception e) { return null; }
    }
    private boolean isManager(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DMANAGER"));
    }
}
