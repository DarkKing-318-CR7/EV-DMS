package com.uth.ev_dms.service.dto;

import com.uth.ev_dms.auth.User;
import lombok.Data;

@Data
public class UserForm {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String roleName;
    private Long dealerId;
    private Long regionId;
    private Boolean enabled;   // ✅ có field này

    // ✅ Thêm method này để controller dùng được f.isEnabled()
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    // ✅ Hàm static từ User -> UserForm
    public static UserForm from(User u) {
        UserForm f = new UserForm();
        f.setId(u.getId());
        f.setUsername(u.getUsername());
        f.setEmail(u.getEmail());
        f.setFullName(u.getFullName());
        f.setEnabled(u.isEnabled());
        if (u.getDealer() != null) f.setDealerId(u.getDealer().getId());
        if (u.getRegion() != null) f.setRegionId(u.getRegion().getId());
        if (u.getRoles() != null && !u.getRoles().isEmpty()) {
            f.setRoleName(u.getRoles().iterator().next().getName());
        }
        return f;
    }
}
