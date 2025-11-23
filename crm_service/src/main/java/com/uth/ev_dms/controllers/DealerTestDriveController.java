package com.uth.ev_dms.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dealer")
public class DealerTestDriveController {

    // Trang dashboard dealer (ví dụ)
    @GetMapping
    public String dashboard() {
        return "dealer/dashboard"; // nếu chưa có view này, có thể tạm trả "redirect:/dealer/test-drive"
        // return "redirect:/dealer/test-drive";
    }

    // TUYỆT ĐỐI KHÔNG có @GetMapping("/test-drive") ở đây nữa!
    // Nếu cần đường dẫn cũ để không 404, đổi nó thành 1 path khác và redirect:
    // @GetMapping("/test-drive-page")
    // public String legacy() {
    //     return "redirect:/dealer/test-drive";
    // }
}
