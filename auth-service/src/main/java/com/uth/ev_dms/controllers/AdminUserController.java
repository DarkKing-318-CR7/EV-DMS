package com.uth.ev_dms.controllers.admin;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.DealerRepo;
import com.uth.ev_dms.service.AdminUserService;
import com.uth.ev_dms.service.dto.UserForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/user-mgmt")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final DealerRepo dealerRepo;

    // ======================= LIST ===========================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", adminUserService.list());
        return "admin/users/list";
    }

    // ======================= CREATE FORM ====================
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("dealers", dealerRepo.findAll());
        return "admin/user-edit";
    }

    // ======================= CREATE =========================
    @PostMapping
    public String create(@ModelAttribute UserForm f, RedirectAttributes ra) {
        Long dealerId = (f.getDealerId() != null && f.getDealerId() == 0) ? null : f.getDealerId();
        boolean enabled = f.getEnabled() == null ? true : f.getEnabled();

        adminUserService.createUser(
                f.getUsername(),
                f.getPassword(),      // rawPassword
                f.getEmail(),
                f.getFullName(),
                f.getRoleName(),
                dealerId,
                enabled
        );

        ra.addFlashAttribute("ok", "Đã tạo user");
        return "redirect:/admin/user-mgmt";
    }

    // ======================= EDIT FORM ======================
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        User u = adminUserService.get(id);
        UserForm f = UserForm.from(u);
        model.addAttribute("userForm", f);
        model.addAttribute("dealers", dealerRepo.findAll());
        return "admin/user-edit";
    }

    // ======================= UPDATE =========================
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute UserForm f, RedirectAttributes ra) {
        Long dealerId = (f.getDealerId() != null && f.getDealerId() == 0) ? null : f.getDealerId();

        adminUserService.updateUser(
                id,
                f.getEmail(),
                f.getFullName(),
                f.getRoleName(),
                dealerId,
                f.getEnabled(),   // có thể null, service tự xử lý
                f.getPassword()   // mật khẩu mới (nếu có nhập)
        );

        ra.addFlashAttribute("ok", "Đã cập nhật user");
        return "redirect:/admin/user-mgmt";
    }
}
