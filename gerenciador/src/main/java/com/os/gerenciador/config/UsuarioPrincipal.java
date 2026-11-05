package com.os.gerenciador.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UsuarioPrincipal(Long id, String email, String senha, String role) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role == null || role.isBlank()
                ? List.of()
                : List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
