package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.service.TestDriveService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/test-drive")
public class TestDriveController {

    private final TestDriveService testDriveService;

    /**
     * ⭐ TẠM THỜI LẤY staffId bằng cách dùng giá trị cố định
     * (vì CRM-service không còn UserService)
     *
     * Sau này bạn sẽ thay bằng JWT từ API-Gateway:
     * Long userId = jwt.getUserId();
     */
    private Long getCurrentUserId(Principal principal) {
        return 1L;  // placeholder for microservice phase 1
    }

    // === DANH SÁCH ===
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("title", "Danh sách lịch lái thử");
        model.addAttribute("list", testDriveService.findAll());
        return "dealer/list";
    }

    // === FORM TẠO LỊCH ===
    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("testDrive", new TestDrive());
        return "dealer/form";
    }

    @PostMapping(value = "/create", consumes = "application/json")
    @ResponseBody
    public String create(@RequestBody TestDrive td) {
        testDriveService.save(td);
        return "OK";
    }

    // === CHI TIẾT (manager hoặc staff) ===
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {

        TestDrive td = testDriveService.get(id);
        if (td == null) {
            return "redirect:/dealer/test-drive/list";
        }

        model.addAttribute("testDrive", td);
        return "dealer/detail";
    }

    // === DUYỆT / HOÀN TẤT / HỦY ===
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        testDriveService.approve(id);
        return "redirect:/dealer/test-drive/list";
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id) {
        testDriveService.complete(id);
        return "redirect:/dealer/test-drive/list";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id) {
        testDriveService.cancel(id);
        return "redirect:/dealer/test-drive/list";
    }

    // === XÓA ===
    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        testDriveService.delete(id);

        if (referer != null && referer.contains("/manager/testdrives")) {
            return "redirect:/manager/testdrives";
        }

        return "redirect:/dealer/test-drive/list";
    }

    // === EXPORT ICS ===
    @GetMapping("/ics/{id}")
    public void exportIcs(@PathVariable Long id, HttpServletResponse response) throws IOException {
        TestDrive td = testDriveService.get(id);
        if (td == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy lịch lái thử");
            return;
        }

        response.setContentType("text/calendar; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=lich-" + td.getId() + ".ics");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String startUtc = td.getScheduleAt().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC).format(fmt);
        String endUtc = td.getScheduleAt().plusHours(1).atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC).format(fmt);

        String notes = td.getNotes() == null ? "" : td.getNotes()
                .replace("\r", "").replace("\n", "\\n");

        String ics =
                "BEGIN:VCALENDAR\r\n" +
                        "VERSION:2.0\r\n" +
                        "PRODID:-//EV-DMS//TestDrive//VN\r\n" +
                        "CALSCALE:GREGORIAN\r\n" +
                        "BEGIN:VEVENT\r\n" +
                        "UID:" + td.getId() + "@evdms.vn\r\n" +
                        "DTSTAMP:" + startUtc + "\r\n" +
                        "DTSTART:" + startUtc + "\r\n" +
                        "DTEND:" + endUtc + "\r\n" +
                        "SUMMARY:Lái thử " + (td.getVehicleName() == null ? "" : td.getVehicleName()) + "\r\n" +
                        "DESCRIPTION:Khách hàng: " + (td.getCustomerName() == null ? "" : td.getCustomerName()) + "\\n" +
                        "Điện thoại: " + (td.getCustomerPhone() == null ? "" : td.getCustomerPhone()) + "\\n" +
                        "Địa điểm: " + (td.getLocation() == null ? "" : td.getLocation()) + "\\n" +
                        "Ghi chú: " + notes + "\r\n" +
                        "STATUS:" + td.getStatus() + "\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR";

        response.getWriter().write(ics);
    }

    // === LỊCH CỦA USER ===
    @GetMapping("/my")
    public String my(@RequestParam(required = false) Long userId, Model model) {
        if (userId != null) {
            model.addAttribute("list", testDriveService.findMineCreated(userId));
        } else {
            model.addAttribute("list", List.of());
        }
        model.addAttribute("title", "Lịch của tôi");
        model.addAttribute("isManager", false);
        return "dealer/list";
    }
}
