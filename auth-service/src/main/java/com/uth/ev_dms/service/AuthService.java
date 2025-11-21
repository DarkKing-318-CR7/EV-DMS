//package com.uth.ev_dms.service;
//
//import com.uth.ev_dms.auth.User;
//import com.uth.ev_dms.repo.UserRepo;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepo userRepo;
//
//    /** Lấy user hiện tại từ SecurityContext */
//    public User getCurrentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth == null || auth.getName() == null) {
//            throw new IllegalStateException("User not authenticated");
//        }
//        return userRepo.findByUsername(auth.getName())
//                .orElseThrow(() -> new IllegalStateException("User not found: " + auth.getName()));
//    }
//
//    /** Lấy dealerId mà user thuộc về (nếu có) */
//    public Long getCurrentDealerId() {
//        User u = getCurrentUser();
//        return u.getDealer() != null ? u.getDealer().getId() : null;
//    }
//
//    /** Lấy branchId mà user thuộc về (nếu có) */
//    public Long getCurrentBranchId() {
//        User u = getCurrentUser();
//        return (u.getDealerBranch() != null)
//                ? u.getDealerBranch().getId()
//                : (u.getDealer() != null && u.getDealer().getMainBranch() != null
//                ? u.getDealer().getMainBranch().getId()
//                : null);
//    }
//}
