package com.uth.ev_dms.admin;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.repo.UserRepository;
import com.uth.ev_dms.repo.RoleRepository;
import com.uth.ev_dms.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AdminDashboardService dashboardService;

    // =============================
    //      DASHBOARD (CHÍNH)
    // =============================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("dealers", dashboardService.totalDealers());
        model.addAttribute("vehicles", dashboardService.totalVehicles());
        model.addAttribute("trims", dashboardService.totalTrims());
        model.addAttribute("promotions", dashboardService.totalPromotions());
        model.addAttribute("orders", dashboardService.totalOrders());
        model.addAttribute("inventory", dashboardService.totalInventory());
        model.addAttribute("users", dashboardService.totalUsers());
        model.addAttribute("customers", dashboardService.totalCustomers());
        model.addAttribute("quotes", dashboardService.totalQuotes());
        model.addAttribute("revenue", dashboardService.totalRevenue());

        return "admin/dashboard";
    }

    // =============================
    //         USER MANAGEMENT
    // =============================
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
    public String saveUser(@PathVariable Long id,
                           @RequestParam(value = "roleIds", required = false) Long[] roleIds) {

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
