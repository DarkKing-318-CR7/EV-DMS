package com.uth.ev_dms.controllers;

import com.uth.ev_dms.service.TestDriveService;
import com.uth.ev_dms.service.UserService;
import com.uth.ev_dms.service.CustomerService;
import com.uth.ev_dms.service.ProductService;
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
    private final UserService userService;
    private final CustomerService customerService;
    private final ProductService productService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Long staffId = userService.findIdByUsername(principal.getName());
        model.addAttribute("list", testDriveService.findMineAssigned(staffId));
        return "dealer/testdrive/list";     // ✔ ĐÚNG THƯ MỤC
    }

    @GetMapping("/create")
    public String createPage(Model model, Principal principal) {

        Long staffId = userService.findIdByUsername(principal.getName());
        Long dealerId = userService.getDealerId(principal);     // ⭐ THÊM

        model.addAttribute("customers", customerService.findByDealer(dealerId)); // ⭐ THÊM

        model.addAttribute("vehicles", productService.getVehiclesWithTrims());
        model.addAttribute("form", new TestDriveCreateForm());
        return "dealer/testdrive/create-staff";
    }


    @PostMapping("/create")
    public String submitCreate(@ModelAttribute("form") TestDriveCreateForm form,
                               Principal principal,
                               RedirectAttributes ra) {

        Long staffId = userService.findIdByUsername(principal.getName());

        // ✅ THÊM 3 DÒNG NÀY
        Long dealerId = userService.getDealerId(principal);
        Long branchId = userService.getBranchId(principal);
        form.setDealerId(dealerId);
        form.setBranchId(branchId);
        form.setStaffId(staffId);

        testDriveService.createByStaff(form, staffId);
        ra.addFlashAttribute("msg", "Tạo lịch lái thử thành công!");
        return "redirect:/staff/testdrives";
    }


    @GetMapping("/calendar")
    public String calendar() {
        return "dealer/testdrive/calendar";    // ✔ ĐÚNG THƯ MỤC
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {

        Long staffDealerId = userService.getDealerId(principal);       // ⭐ THÊM
        Long tdDealerId = testDriveService.get(id).getDealer().getId();  // ⭐ THÊM
        if (!staffDealerId.equals(tdDealerId))                         // ⭐ THÊM
            throw new RuntimeException("Bạn không có quyền xem lịch của đại lý khác!");

        model.addAttribute("item", testDriveService.get(id));
        return "dealer/testdrive/detail";
    }


    @GetMapping("/api/events")
    @ResponseBody
    public List<Map<String, Object>> events(Principal principal) {
        Long staffId = userService.findIdByUsername(principal.getName());
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
