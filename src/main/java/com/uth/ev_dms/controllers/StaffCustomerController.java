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
@RequestMapping("/staff/customers")
public class StaffCustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String myList(@RequestParam(required = false) String q,
                         Authentication auth,
                         Model model) {

        Long userId = currentUserId(auth);

        var list = (q != null && !q.isBlank())
                ? customerService.searchMine(userId, q)
                : customerService.findMine(userId);

        model.addAttribute("list", list);
        model.addAttribute("q", q);

        // ⭐ STAFF view
        return "dealer/customers/my-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("c", new Customer());
        return "dealer/customers/form";
    }

    @PostMapping
    public String create(@ModelAttribute("c") Customer c,
                         Authentication auth,
                         RedirectAttributes ra) {

        c.setOwnerId(currentUserId(auth));
        customerService.create(c);

        ra.addFlashAttribute("msg", "Đã tạo khách hàng");
        return "redirect:/staff/customers";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Authentication auth,
                         Model model,
                         RedirectAttributes ra) {

        Customer c = customerService.findById(id);
        if (c == null) {
            ra.addFlashAttribute("msg", "Không tìm thấy");
            return "redirect:/staff/customers";
        }

        if (!c.getOwnerId().equals(currentUserId(auth))) {
            ra.addFlashAttribute("msg", "Không đủ quyền");
            return "redirect:/staff/customers";
        }

        model.addAttribute("c", c);

        return "dealer/customers/detail";
    }

    private Long currentUserId(Authentication auth) {
        try {
            return Long.parseLong(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
