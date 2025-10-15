package com.uth.ev_dms.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() { return "home"; } // hoặc "dashboard-admin"

    @GetMapping("/dealers")
    public String dealers() { return "dealers"; }

    @GetMapping("/users")
    public String users() { return "users"; }

    @GetMapping("/settings")
    public String settings() { return "settings"; }

    @GetMapping("/reports")
    public String reports() { return "reports"; }

    @GetMapping("/support")
    public String support() { return "support"; }
}
