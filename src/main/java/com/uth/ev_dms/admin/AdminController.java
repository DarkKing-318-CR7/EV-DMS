package com.uth.ev_dms.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() { return "admin/dashboard"; } // templates/admin/dashboard.html

    @GetMapping("/dealers")
    public String dealers() { return "admin/dealers"; }

    @GetMapping("/users")
    public String users() { return "admin/users"; }

    @GetMapping("/settings")
    public String settings() { return "admin/settings"; }

    @GetMapping("/reports")
    public String reports() { return "admin/reports"; }

    @GetMapping("/support")
    public String support() { return "admin/support"; }

}
