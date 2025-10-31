package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDrive;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dealer/test-drive")
public class TestDriveController {

    // NOTE: Tạm thời không gọi service để tránh lỗi method not found
    // private final TestDriveService testDriveService;
    // public TestDriveController(TestDriveService testDriveService) { this.testDriveService = testDriveService; }

    @GetMapping("/form")
    public String showForm() {
        return "dealer/test-drive/form";
    }

    @GetMapping("/detail/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        // Placeholder so the view renders without service calls
        TestDrive td = new TestDrive();
        td.setId(id);
        model.addAttribute("td", td);
        return "dealer/test-drive/detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        // TODO: integrate service when its API is finalized
        return "redirect:/dealer/test-drive/detail/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        // TODO: integrate service when its API is finalized
        return "redirect:/dealer/test-drive/detail/" + id;
    }

    @PostMapping("/{id}/done")
    public String done(@PathVariable Long id) {
        // TODO: integrate service when its API is finalized
        return "redirect:/dealer/test-drive/detail/" + id;
    }
}
