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

    // =========================
    // LIST TẤT CẢ KHÁCH (MANAGER)
    // =========================
    @GetMapping
    public String listAll(@RequestParam(required = false) String q,
                          Model model) {

        var list = (q != null && !q.isBlank())
                ? customerService.searchAll(q)
                : customerService.findAll();

        model.addAttribute("list", list);
        model.addAttribute("q", q);

        // 🔥 FILE: templates/dealer/customers.html
        return "dealer/customers";
    }

    // =========================
    // LIST KHÁCH CỦA STAFF (MY CUSTOMERS)
    // =========================
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

        // 🔥 FILE: templates/dealer/customers-page/my.html
        return "dealer/customers-page/my";
    }

    // =========================
    // FORM TẠO KHÁCH
    // =========================
    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("c", new Customer());

        // 🔥 FILE: templates/dealer/form.html
        return "dealer/customers-page/form";
    }

    // =========================
    // TẠO KHÁCH
    // =========================
    @PostMapping
    public String create(@ModelAttribute("c") Customer c,
                         Authentication auth,
                         RedirectAttributes ra) {

        c.setOwnerId(currentUserId(auth));
        customerService.create(c);

        ra.addFlashAttribute("msg", "Đã tạo khách hàng thành công!");
        return "redirect:/dealer/customers/my";
    }

    // =========================
    // XEM CHI TIẾT
    // =========================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Authentication auth,
                         Model model,
                         RedirectAttributes ra) {

        Customer c = customerService.findById(id);
        if (c == null) {
            ra.addFlashAttribute("msg", "Không tìm thấy khách!");
            return "redirect:/dealer/customers/my";
        }

        if (!isManager(auth) && !c.getOwnerId().equals(currentUserId(auth))) {
            ra.addFlashAttribute("msg", "Không có quyền xem khách này");
            return "redirect:/dealer/customers/my";
        }

        model.addAttribute("c", c);

        // 🔥 FILE: templates/dealer/detail.html
        return "dealer/detail";
    }

    // =========================
    // UPDATE
    // =========================
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("c") Customer c,
                         Authentication auth,
                         RedirectAttributes ra) {

        Customer old = customerService.findById(id);
        if (old == null) {
            ra.addFlashAttribute("msg", "Không tìm thấy khách");
            return "redirect:/dealer/customers/my";
        }

        if (!isManager(auth) && !old.getOwnerId().equals(currentUserId(auth))) {
            ra.addFlashAttribute("msg", "Không có quyền");
            return "redirect:/dealer/customers/my";
        }

        c.setId(id);
        c.setOwnerId(old.getOwnerId());
        customerService.update(c);

        ra.addFlashAttribute("msg", "Đã cập nhật khách hàng");
        return "redirect:/dealer/customers/" + id;
    }

    // =========================
    // DELETE
    // =========================
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         Authentication auth,
                         RedirectAttributes ra) {

        Customer old = customerService.findById(id);

        if (old != null &&
                (isManager(auth) || old.getOwnerId().equals(currentUserId(auth)))) {
            customerService.delete(id);
            ra.addFlashAttribute("msg", "Đã xóa khách");
        } else {
            ra.addFlashAttribute("msg", "Không có quyền xóa");
        }

        return "redirect:/dealer/customers/my";
    }

    // =========================
    // HELPER
    // =========================
    private Long currentUserId(Authentication auth) {
        try { return Long.parseLong(auth.getName()); }
        catch (Exception e) { return null; }
    }

    private boolean isManager(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DMANAGER"));
    }
}
