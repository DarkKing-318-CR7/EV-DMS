package com.uth.ev_dms.admin;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.UserRepository;
import com.uth.ev_dms.repo.RoleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class

AdminController {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AdminController(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @GetMapping("/dashboard")
    public String home(){
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepo.findById(id).orElse(null));
        model.addAttribute("roles", roleRepo.findAll());
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}/save")
    public String saveUser(@PathVariable Long id, @RequestParam(value="roleIds", required=false) Long[] roleIds) {
        User u = userRepo.findById(id).orElse(null);
        if (u != null) {
            u.getRoles().clear();
            if (roleIds != null) {
                for (Long rid : roleIds) {
                    roleRepo.findById(rid).ifPresent(u.getRoles()::add);
                }
            }
            userRepo.save(u);
        }
        return "redirect:/admin/users";
    }
}
