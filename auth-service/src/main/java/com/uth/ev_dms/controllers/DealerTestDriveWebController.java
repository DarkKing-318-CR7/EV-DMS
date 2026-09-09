package com.uth.ev_dms.controllers;

import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.TestDrive;
import com.uth.ev_dms.domain.TestDriveStatus;
import com.uth.ev_dms.repo.TestDriveRepo;
import com.uth.ev_dms.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DealerTestDriveWebController {

    private final TestDriveRepo testDriveRepo;
    private final UserRepo userRepo;

    @GetMapping({"/dealer/test-drive", "/manager/testdrives", "/staff/testdrives"})
    public String listTestDrives(
            @RequestParam(name = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(name = "status", required = false) TestDriveStatus status,
            Model model,
            Authentication auth
    ) {
        List<TestDrive> list;
        if (status != null) {
            list = testDriveRepo.findByStatusOrderByScheduleAt(status);
        } else {
            list = testDriveRepo.findAllByOrderByScheduleAt();
            if (list.isEmpty()) {
                list = testDriveRepo.findAll();
            }
        }

        List<User> staffs = userRepo.findAll();

        model.addAttribute("items", list);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("staffs", staffs);
        model.addAttribute("title", "Lịch lái thử");
        model.addAttribute("active", "testdrive");

        return "dealer/test-drive";
    }

    @PostMapping({"/manager/testdrives/{id}/status", "/dealer/test-drive/{id}/status"})
    @Transactional
    public String updateStatus(@PathVariable(name = "id") Long id,
                               @RequestParam(name = "status") TestDriveStatus status,
                               RedirectAttributes ra) {
        TestDrive td = testDriveRepo.findById(id).orElse(null);
        if (td != null) {
            td.setStatus(status);
            testDriveRepo.save(td);
            ra.addFlashAttribute("ok", "Cập nhật trạng thái lái thử thành công.");
        }
        return "redirect:/dealer/test-drive";
    }

    @PostMapping({"/manager/testdrives/{id}/assign", "/dealer/test-drive/{id}/assign"})
    @Transactional
    public String assignStaff(@PathVariable(name = "id") Long id,
                              @RequestParam(name = "staffId") Long staffId,
                              RedirectAttributes ra) {
        TestDrive td = testDriveRepo.findById(id).orElse(null);
        User staff = userRepo.findById(staffId).orElse(null);
        if (td != null && staff != null) {
            td.setAssignedStaff(staff);
            testDriveRepo.save(td);
            ra.addFlashAttribute("ok", "Giao nhân viên phụ trách thành công.");
        }
        return "redirect:/dealer/test-drive";
    }

    @PostMapping({"/dealer/testdrive/create", "/manager/testdrives/create"})
    @Transactional
    public String createTestDrive(
            @RequestParam(name = "customerName") String customerName,
            @RequestParam(name = "customerPhone", required = false) String customerPhone,
            @RequestParam(name = "vehicleName") String vehicleName,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "scheduleAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduleAt,
            @RequestParam(name = "notes", required = false) String notes,
            Authentication auth,
            RedirectAttributes ra
    ) {
        TestDrive td = new TestDrive();
        td.setCustomerName(customerName);
        td.setCustomerPhone(customerPhone);
        td.setVehicleName(vehicleName);
        td.setLocation(location != null ? location : "Đại lý");
        td.setScheduleAt(scheduleAt != null ? scheduleAt : LocalDateTime.now().plusDays(1));
        td.setNotes(notes);
        td.setStatus(TestDriveStatus.REQUESTED);

        if (auth != null) {
            userRepo.findByUsername(auth.getName()).ifPresent(td::setCreatedBy);
        }

        testDriveRepo.save(td);
        ra.addFlashAttribute("ok", "Tạo lịch hẹn lái thử thành công.");
        return "redirect:/dealer/test-drive";
    }
}
