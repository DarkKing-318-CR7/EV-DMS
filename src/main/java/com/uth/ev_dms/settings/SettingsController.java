package com.uth.ev_dms.settings;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.uth.ev_dms.user.UserEntity;
import com.uth.ev_dms.user.UserRepository;

@Controller
public class SettingsController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public SettingsController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @GetMapping("/settings")
    public String settings(@AuthenticationPrincipal User principal, Model model) {
        if (principal == null) return "redirect:/login";
        UserEntity u = userRepo.findByUsername(principal.getUsername()).orElse(null);
        model.addAttribute("user", u);
        return "settings/settings";
    }

    @PostMapping("/settings/password")
    public String changePassword(@AuthenticationPrincipal User principal,
                                 @RequestParam String newPassword, Model model) {
        if (principal == null) return "redirect:/login";
        UserEntity u = userRepo.findByUsername(principal.getUsername()).orElse(null);
        if (u != null) {
            u.setPassword(encoder.encode(newPassword));
            userRepo.save(u);
            model.addAttribute("success", "Đổi mật khẩu thành công");
        }
        model.addAttribute("user", u);
        return "settings/settings";
    }
}
