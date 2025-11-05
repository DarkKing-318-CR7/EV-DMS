package com.uth.ev_dms.auth;

import com.uth.ev_dms.domain.Dealer;
import com.uth.ev_dms.domain.Region;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users") // khớp tên bảng của bạn
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Builder.Default
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",               // khớp bảng link của bạn
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @ManyToOne
    @JoinColumn(name = "dealer_id")  // cột FK trong bảng users
    private Dealer dealer;

    @ManyToOne
    @JoinColumn(name = "region_id")  // cột FK trong bảng users
    private Region region;
}
