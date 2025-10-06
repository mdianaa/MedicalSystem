package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {


    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<String> authorities;

    private boolean enabled = false;
    private boolean locked = false;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Patient patientProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Doctor doctorProfile;

    // ---- UserDetails ----
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? Set.<SimpleGrantedAuthority>of()
                : authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !locked; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

}