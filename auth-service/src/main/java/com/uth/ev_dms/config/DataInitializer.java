package com.uth.ev_dms.config;

import com.uth.ev_dms.auth.Role;
import com.uth.ev_dms.auth.User;
import com.uth.ev_dms.domain.*;
import com.uth.ev_dms.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final DealerRepo dealerRepo;
    private final DealerBranchRepo branchRepo;
    private final VehicleRepo vehicleRepo;
    private final TrimRepo trimRepo;
    private final PriceListRepo priceListRepo;
    private final InventoryRepo inventoryRepo;
    private final CustomerRepo customerRepo;
    private final PromotionRepo promotionRepo;
    private final OrderRepo orderHdrRepo;
    private final TestDriveRepo testDriveRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roleRepo.count() > 0 && userRepo.count() > 0) {
            log.info("Database already seeded. Skipping initial data loading.");
            return;
        }

        log.info("Seeding initial EV-DMS database...");

        // 1. Roles
        Role adminRole = roleRepo.findByName("ROLE_ADMIN");
        if (adminRole == null) adminRole = roleRepo.save(Role.builder().name("ROLE_ADMIN").build());

        Role managerRole = roleRepo.findByName("ROLE_DEALER_MANAGER");
        if (managerRole == null) managerRole = roleRepo.save(Role.builder().name("ROLE_DEALER_MANAGER").build());

        Role staffRole = roleRepo.findByName("ROLE_DEALER_STAFF");
        if (staffRole == null) staffRole = roleRepo.save(Role.builder().name("ROLE_DEALER_STAFF").build());

        Role evmRole = roleRepo.findByName("ROLE_EVM_STAFF");
        if (evmRole == null) evmRole = roleRepo.save(Role.builder().name("ROLE_EVM_STAFF").build());

        // 2. Dealers & Branches
        Dealer dealer1 = dealerRepo.findByCode("DLR-HN").orElse(null);
        if (dealer1 == null) {
            dealer1 = dealerRepo.save(Dealer.builder()
                    .code("DLR-HN")
                    .name("VinFast Đại Việt - Hà Nội")
                    .region("North")
                    .province("Hà Nội")
                    .phone("024-12345678")
                    .email("hn@evdms.com")
                    .active(true)
                    .status(Dealer.Status.ACTIVE)
                    .build());
        }

        DealerBranch branch1 = branchRepo.findByDealerId(dealer1.getId()).orElse(null);
        if (branch1 == null) {
            branch1 = branchRepo.save(DealerBranch.builder()
                    .dealer(dealer1)
                    .code("MAIN")
                    .name("VinFast Đại Việt - Main Showroom")
                    .province("Hà Nội")
                    .phone("024-12345678")
                    .email("hn-showroom@evdms.com")
                    .status(DealerBranch.Status.ACTIVE)
                    .build());
        }

        Dealer dealer2 = dealerRepo.findByCode("DLR-HCM").orElse(null);
        if (dealer2 == null) {
            dealer2 = dealerRepo.save(Dealer.builder()
                    .code("DLR-HCM")
                    .name("VinFast Sài Gòn - TP.HCM")
                    .region("South")
                    .province("TP. Hồ Chí Minh")
                    .phone("028-87654321")
                    .email("hcm@evdms.com")
                    .active(true)
                    .status(Dealer.Status.ACTIVE)
                    .build());
        }

        // 3. Users
        String encodedPass = passwordEncoder.encode("123456");

        if (userRepo.findByUsername("admin").isEmpty()) {
            userRepo.save(User.builder()
                    .username("admin")
                    .password(encodedPass)
                    .fullName("System Administrator")
                    .email("admin@evdms.com")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build());
        }

        User manager1 = userRepo.findByUsername("manager1").orElse(null);
        if (manager1 == null) {
            manager1 = userRepo.save(User.builder()
                    .username("manager1")
                    .password(encodedPass)
                    .fullName("Trần Quản Lý (Dealer Manager)")
                    .email("managerA@evdms.com")
                    .enabled(true)
                    .dealer(dealer1)
                    .roles(new HashSet<>(Set.of(managerRole)))
                    .build());
        }

        User staff1 = userRepo.findByUsername("staff1").orElse(null);
        if (staff1 == null) {
            staff1 = userRepo.save(User.builder()
                    .username("staff1")
                    .password(encodedPass)
                    .fullName("Lê Nhân Viên (Dealer Staff)")
                    .email("staffA@evdms.com")
                    .enabled(true)
                    .dealer(dealer1)
                    .roles(new HashSet<>(Set.of(staffRole)))
                    .build());
        }

        if (userRepo.findByUsername("evm1").isEmpty()) {
            userRepo.save(User.builder()
                    .username("evm1")
                    .password(encodedPass)
                    .fullName("Hoàng EVM (Hãng xe)")
                    .email("evmA@evdms.com")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(evmRole)))
                    .build());
        }

        // 4. Vehicles & Trims & Prices
        if (vehicleRepo.count() == 0) {
            // Vehicle 1: VF 8
            Vehicle vf8 = new Vehicle();
            vf8.setModelCode("VF8");
            vf8.setModelName("VinFast VF 8");
            vf8.setBrand("VinFast");
            vf8.setBodyType("D-SUV");
            vf8.setWarrantyMonths(120);
            vf8 = vehicleRepo.save(vf8);

            Trim vf8Eco = trimRepo.save(Trim.builder()
                    .vehicle(vf8)
                    .trimName("VF 8 Eco")
                    .batteryKWh(87)
                    .rangeKm(471)
                    .powerHp(348)
                    .drive(DriveType.AWD)
                    .build());

            priceListRepo.save(PriceList.builder()
                    .trim(vf8Eco)
                    .msrp(new BigDecimal("1090000000"))
                    .effectiveFrom(LocalDate.of(2025, 1, 1))
                    .active(true)
                    .currency("VND")
                    .build());

            Trim vf8Plus = trimRepo.save(Trim.builder()
                    .vehicle(vf8)
                    .trimName("VF 8 Plus")
                    .batteryKWh(87)
                    .rangeKm(447)
                    .powerHp(402)
                    .drive(DriveType.AWD)
                    .build());

            priceListRepo.save(PriceList.builder()
                    .trim(vf8Plus)
                    .msrp(new BigDecimal("1270000000"))
                    .effectiveFrom(LocalDate.of(2025, 1, 1))
                    .active(true)
                    .currency("VND")
                    .build());

            // Vehicle 2: VF 9
            Vehicle vf9 = new Vehicle();
            vf9.setModelCode("VF9");
            vf9.setModelName("VinFast VF 9");
            vf9.setBrand("VinFast");
            vf9.setBodyType("E-SUV");
            vf9.setWarrantyMonths(120);
            vf9 = vehicleRepo.save(vf9);

            Trim vf9Eco = trimRepo.save(Trim.builder()
                    .vehicle(vf9)
                    .trimName("VF 9 Eco")
                    .batteryKWh(123)
                    .rangeKm(594)
                    .powerHp(402)
                    .drive(DriveType.AWD)
                    .build());

            priceListRepo.save(PriceList.builder()
                    .trim(vf9Eco)
                    .msrp(new BigDecimal("1491000000"))
                    .effectiveFrom(LocalDate.of(2025, 1, 1))
                    .active(true)
                    .currency("VND")
                    .build());

            Trim vf9Plus = trimRepo.save(Trim.builder()
                    .vehicle(vf9)
                    .trimName("VF 9 Plus")
                    .batteryKWh(123)
                    .rangeKm(580)
                    .powerHp(402)
                    .drive(DriveType.AWD)
                    .build());

            priceListRepo.save(PriceList.builder()
                    .trim(vf9Plus)
                    .msrp(new BigDecimal("1685000000"))
                    .effectiveFrom(LocalDate.of(2025, 1, 1))
                    .active(true)
                    .currency("VND")
                    .build());

            // Vehicle 3: VF 5
            Vehicle vf5 = new Vehicle();
            vf5.setModelCode("VF5");
            vf5.setModelName("VinFast VF 5 Plus");
            vf5.setBrand("VinFast");
            vf5.setBodyType("A-SUV");
            vf5.setWarrantyMonths(84);
            vf5 = vehicleRepo.save(vf5);

            Trim vf5Plus = trimRepo.save(Trim.builder()
                    .vehicle(vf5)
                    .trimName("VF 5 Plus Standard")
                    .batteryKWh(37)
                    .rangeKm(326)
                    .powerHp(134)
                    .drive(DriveType.FWD)
                    .build());

            priceListRepo.save(PriceList.builder()
                    .trim(vf5Plus)
                    .msrp(new BigDecimal("468000000"))
                    .effectiveFrom(LocalDate.of(2025, 1, 1))
                    .active(true)
                    .currency("VND")
                    .build());

            // 5. Inventories
            inventoryRepo.save(Inventory.builder()
                    .dealer(dealer1)
                    .branch(branch1)
                    .trim(vf8Eco)
                    .qtyOnHand(10)
                    .reserved(2)
                    .locationType("SHOWROOM")
                    .build());

            inventoryRepo.save(Inventory.builder()
                    .dealer(dealer1)
                    .branch(branch1)
                    .trim(vf8Plus)
                    .qtyOnHand(6)
                    .reserved(1)
                    .locationType("SHOWROOM")
                    .build());

            inventoryRepo.save(Inventory.builder()
                    .dealer(dealer1)
                    .branch(branch1)
                    .trim(vf9Eco)
                    .qtyOnHand(4)
                    .reserved(0)
                    .locationType("WAREHOUSE")
                    .build());

            inventoryRepo.save(Inventory.builder()
                    .dealer(dealer1)
                    .branch(branch1)
                    .trim(vf5Plus)
                    .qtyOnHand(15)
                    .reserved(3)
                    .locationType("SHOWROOM")
                    .build());
        }

        // 6. Customers
        if (customerRepo.count() == 0) {
            Customer c1 = new Customer();
            c1.setTen("Nguyễn Văn An");
            c1.setSdt("0905123456");
            c1.setEmail("an.nguyen@example.com");
            c1.setDiachi("12 Nguyễn Trãi, Thanh Xuân, Hà Nội");
            c1.setOwnerId(staff1 != null ? staff1.getId() : 1L);
            customerRepo.save(c1);

            Customer c2 = new Customer();
            c2.setTen("Trần Thị Bình");
            c2.setSdt("0912345678");
            c2.setEmail("binh.tran@example.com");
            c2.setDiachi("45 Lê Lợi, Ba Đình, Hà Nội");
            c2.setOwnerId(staff1 != null ? staff1.getId() : 1L);
            customerRepo.save(c2);

            Customer c3 = new Customer();
            c3.setTen("Lê Minh Cường");
            c3.setSdt("0987654321");
            c3.setEmail("cuong.le@example.com");
            c3.setDiachi("23 Cầu Giấy, Cầu Giấy, Hà Nội");
            c3.setOwnerId(staff1 != null ? staff1.getId() : 1L);
            customerRepo.save(c3);

            Customer c4 = new Customer();
            c4.setTen("Phạm Thu Dung");
            c4.setSdt("0909888777");
            c4.setEmail("dung.pham@example.com");
            c4.setDiachi("78 Hai Bà Trưng, Hoàn Kiếm, Hà Nội");
            c4.setOwnerId(manager1 != null ? manager1.getId() : 1L);
            customerRepo.save(c4);
        }

        // 7. Promotions
        if (promotionRepo.count() == 0) {
            Promotion p1 = new Promotion();
            p1.setName("Khuyến mãi xuân 2025");
            p1.setTitle("Hỗ trợ lệ phí trước bạ 100%");
            p1.setDescription("Hỗ trợ toàn bộ lệ phí trước bạ khi mua dòng xe VF 8 và VF 9.");
            p1.setDiscountPercent(new BigDecimal("10.00"));
            p1.setDiscountRate(0.10);
            p1.setStartDate(LocalDate.now().minusDays(15));
            p1.setEndDate(LocalDate.now().plusDays(45));
            p1.setActive(true);
            p1.setBudget(new BigDecimal("5000000000"));
            promotionRepo.save(p1);

            Promotion p2 = new Promotion();
            p2.setName("Gói sạc pin miễn phí 1 năm");
            p2.setTitle("Miễn phí trạm sạc V-GREEN");
            p2.setDescription("Miễn phí sạc pin tại trạm sạc công cộng trong vòng 12 tháng.");
            p2.setDiscountPercent(new BigDecimal("5.00"));
            p2.setDiscountRate(0.05);
            p2.setStartDate(LocalDate.now().minusDays(5));
            p2.setEndDate(LocalDate.now().plusDays(90));
            p2.setActive(true);
            p2.setBudget(new BigDecimal("2000000000"));
            promotionRepo.save(p2);
        }

        // 8. Test Drives
        if (testDriveRepo.count() == 0) {
            TestDrive td1 = new TestDrive();
            td1.setCustomerName("Nguyễn Văn An");
            td1.setCustomerPhone("0905123456");
            td1.setVehicleName("VinFast VF 8 Plus");
            td1.setLocation("Đại lý VinFast Đại Việt - Hà Nội");
            td1.setScheduleAt(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
            td1.setNotes("Khách quan tâm bản cao cấp màu xanh");
            td1.setStatus(TestDriveStatus.REQUESTED);
            td1.setCreatedBy(staff1);
            testDriveRepo.save(td1);

            TestDrive td2 = new TestDrive();
            td2.setCustomerName("Trần Thị Bình");
            td2.setCustomerPhone("0912345678");
            td2.setVehicleName("VinFast VF 5 Plus");
            td2.setLocation("Đại lý VinFast Đại Việt - Hà Nội");
            td2.setScheduleAt(LocalDateTime.now().plusDays(2).withHour(14).withMinute(30));
            td2.setNotes("Lái thử trong thành phố");
            td2.setStatus(TestDriveStatus.CONFIRMED);
            td2.setCreatedBy(staff1);
            td2.setAssignedStaff(staff1);
            testDriveRepo.save(td2);
        }

        // 9. Sample Orders
        if (orderHdrRepo.count() == 0) {
            OrderHdr o1 = new OrderHdr();
            o1.setOrderNo("ORD-2025-001");
            o1.setDealerId(dealer1.getId());
            o1.setCustomerId(1L);
            o1.setSalesStaffId(staff1 != null ? staff1.getId() : 1L);
            o1.setCreatedBy(staff1 != null ? staff1.getId() : 1L);
            o1.setStatus(OrderStatus.NEW);
            o1.setTotalAmount(new BigDecimal("1270000000"));
            o1.setDepositAmount(new BigDecimal("50000000"));
            o1.setPaidAmount(new BigDecimal("50000000"));
            o1.setBalanceAmount(new BigDecimal("1220000000"));
            o1.setSubmittedAt(LocalDateTime.now().minusDays(2));
            orderHdrRepo.save(o1);

            OrderHdr o2 = new OrderHdr();
            o2.setOrderNo("ORD-2025-002");
            o2.setDealerId(dealer1.getId());
            o2.setCustomerId(2L);
            o2.setSalesStaffId(staff1 != null ? staff1.getId() : 1L);
            o2.setCreatedBy(staff1 != null ? staff1.getId() : 1L);
            o2.setStatus(OrderStatus.ALLOCATED);
            o2.setTotalAmount(new BigDecimal("468000000"));
            o2.setDepositAmount(new BigDecimal("30000000"));
            o2.setPaidAmount(new BigDecimal("468000000"));
            o2.setBalanceAmount(BigDecimal.ZERO);
            o2.setSubmittedAt(LocalDateTime.now().minusDays(5));
            o2.setAllocatedAt(LocalDateTime.now().minusDays(1));
            orderHdrRepo.save(o2);
        }

        log.info("EV-DMS initial data seeding completed successfully!");
    }
}
