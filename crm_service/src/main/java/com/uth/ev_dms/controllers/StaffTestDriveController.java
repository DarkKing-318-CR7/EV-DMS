package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.CustomerService;
import com.uth.ev_dms.service.dto.TestDriveCreateForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/testdrives")
public class StaffTestDriveController {

    private final TestDriveService testDriveService;
    private final CustomerService customerService;

    // ============================================================
    // LẤY staffId — vì KHÔNG còn UserService trong microservice
    // ============================================================
    private Long getStaffId(Principal principal) {
        // ❗ TẠM THỜI: principal.getName() chính là userId
        // Trong bước sau bạn sẽ dùng JWT để lấy userId chuẩn.
        try {
            return Long.parseLong(principal.getName());
        } catch (Exception ex) {
            return 1L; // fallback để chạy được
        }
    }

    // ============================================================
    // LIST LỊCH LÁI THỬ CỦA STAFF
    // ============================================================
    @GetMapping
    public String list(Model model, Principal principal) {
        Long staffId = getStaffId(principal);
        model.addAttribute("list", testDriveService.findMineAssigned(staffId));
        return "dealer/testdrive/list";
    }

    // ============================================================
    // FORM TẠO LỊCH LÁI THỬ
    // ============================================================
    @GetMapping("/create")
    public String createPage(Model model) {

        model.addAttribute("customers", customerService.findAll());
        // CRM không còn productService → tạm thời bỏ
        model.addAttribute("vehicles", List.of());
        model.addAttribute("form", new TestDriveCreateForm());

        return "dealer/testdrive/create-staff";
    }

    // ============================================================
    // SUBMIT TẠO LỊCH LÁI THỬ
    // ============================================================
    @PostMapping("/create")
    public String submitCreate(
            @ModelAttribute("form") TestDriveCreateForm form,
            Principal principal,
            RedirectAttributes ra
    ) {
        Long staffId = getStaffId(principal);

        try {
            testDriveService.createByStaff(form, staffId);
            ra.addFlashAttribute("msg", "Tạo lịch lái thử thành công!");
            return "redirect:/staff/testdrives";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/testdrives/create";
        }
    }

    // ============================================================
    // CALENDAR
    // ============================================================
    @GetMapping("/calendar")
    public String calendar() {
        return "dealer/testdrive/calendar";
    }

    // ============================================================
    // CHI TIẾT
    // ============================================================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("item", testDriveService.get(id));
        return "dealer/testdrive/detail";
    }

    // ============================================================
    // API EVENTS (FULLCALENDAR)
    // ============================================================
    @GetMapping("/api/events")
    @ResponseBody
    public List<Map<String, Object>> events(Principal principal) {

        Long staffId = getStaffId(principal);

        return testDriveService.findMineAssigned(staffId)
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "title", t.getCustomerName(),
                        "start", t.getStartTime().toString(),
                        "end", t.getEndTime().toString()
                ))
                .toList();
    }
}
