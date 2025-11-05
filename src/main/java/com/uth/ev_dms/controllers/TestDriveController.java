package com.uth.ev_dms.controllers;

import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.service.TestDriveService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dealer/test-drive")
public class TestDriveController {

    private final TestDriveService testDriveService;

    // === TRANG FORM TẠO LỊCH ===
    // Danh sách
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("title", "Danh sách lịch lái thử");
        model.addAttribute("list", testDriveService.findAll());
        return "dealer/list";              // file: src/main/resources/templates/dealer/list.html
    }

    // Form tạo lịch
    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("testDrive", new TestDrive());
        return "dealer/form";              // file: templates/dealer/form.html
    }

    // Lưu
    // Lưu từ JSON (AJAX)
    @PostMapping(value = "/create", consumes = "application/json")
    @ResponseBody
    public String create(@RequestBody TestDrive td) {
        // nếu có LocalDateTime scheduleAt thì cần định dạng ở entity (xem Sửa 2)
        testDriveService.save(td);
        return "OK"; // trả 200 cho fetch().ok
    }


    // Chi tiết
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("testDrive", testDriveService.get(id));
        return "dealer/detail";            // file: templates/dealer/detail.html
    }

    // Duyệt
    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id) {
        testDriveService.approve(id);
        return "redirect:/dealer/list"; // ← đúng
    }

    // Hoàn tất
    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Long id) {
        testDriveService.complete(id);
        return "redirect:/dealer/list"; // ← đúng
    }

    // Hủy
    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id) {
        testDriveService.cancel(id);
        return "redirect:/dealer/list"; // ← đúng
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestHeader(value = "Referer", required = false) String referer) {
        testDriveService.delete(id);

        // Nếu người dùng đến từ trang Manager thì quay lại Manager
        if (referer != null && referer.contains("/manager/testdrives")) {
            return "redirect:/manager/testdrives";
        }

        // Mặc định quay về trang Dealer List
        return "redirect:/dealer/list";
    }
    @GetMapping("/ics/{id}")
    public void exportIcs(@PathVariable Long id, HttpServletResponse response) throws IOException {
        com.uth.ev_dms.domain.TestDrive td = testDriveService.get(id);
        if (td == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Khong tim thay lich lai thu");
            return;
        }

        response.setContentType("text/calendar; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=lich-" + td.getId() + ".ics");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String startUtc = td.getScheduleAt()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(fmt);
        String endUtc = td.getScheduleAt().plusHours(1)
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(fmt);

        String notes = td.getNotes() == null ? "" : td.getNotes().replace("\r", "").replace("\n", "\\n");
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
                        "SUMMARY:Lai thu " + (td.getVehicleName() == null ? "" : td.getVehicleName()) + "\r\n" +
                        "DESCRIPTION:Khach hang: " + (td.getCustomerName() == null ? "" : td.getCustomerName()) + "\\n" +
                        "Dien thoai: " + (td.getCustomerPhone() == null ? "" : td.getCustomerPhone()) + "\\n" +
                        "Dia diem: " + (td.getLocation() == null ? "" : td.getLocation()) + "\\n" +
                        "Ghi chu: " + notes + "\r\n" +
                        "STATUS:" + td.getStatus() + "\r\n" +
                        "END:VEVENT\r\n" +
                        "END:VCALENDAR";

        response.getWriter().write(ics);
    }
    @GetMapping("/my")
    public String my(@RequestParam(required = false) Long userId, Model model) {
        if (userId != null) {
            model.addAttribute("list", testDriveService.findMineCreated(userId));
        } else {
            model.addAttribute("list", java.util.Collections.<com.uth.ev_dms.domain.TestDrive>emptyList());
        }
        model.addAttribute("title", "Lich cua toi");
        model.addAttribute("isManager", false);
        return "dealer/list";
    }
}
