package com.uth.ev_dms.admin;


import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/admin/dealers")
@PreAuthorize("hasAnyRole('ADMIN','EVM_STAFF')")
@RequiredArgsConstructor
public class AdminDealerController {
    private final DealerService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activePage", "admin-dealers");
        model.addAttribute("dealers", service.list());
        return "admin/dealers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("activePage", "admin-dealers");
        model.addAttribute("dealer", new Dealer());
        model.addAttribute("formTitle", "Create Dealer");
        model.addAttribute("formAction", "/admin/dealers/create");
        return "admin/dealers/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Dealer dealer, RedirectAttributes ra) {
        service.create(dealer);
        ra.addFlashAttribute("msg", "Dealer created (Main branch auto-created)");
        return "redirect:/admin/dealers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("activePage", "admin-dealers");
        model.addAttribute("dealer", service.get(id));
        model.addAttribute("formTitle", "Edit Dealer");
        model.addAttribute("formAction", "/admin/dealers/" + id + "/edit");
        return "admin/dealers/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Dealer dealer, RedirectAttributes ra) {
        service.update(id, dealer);
        ra.addFlashAttribute("msg", "Dealer updated");
        return "redirect:/admin/dealers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Dealer deleted");
        return "redirect:/admin/dealers";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("activePage", "admin-dealers");
        model.addAttribute("dealer", service.get(id));
        return "admin/dealers/detail";
    }
}

