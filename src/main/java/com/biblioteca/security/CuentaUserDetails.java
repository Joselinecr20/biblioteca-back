package com.biblioteca.security;

import com.biblioteca.model.Cuenta;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CuentaUserDetails implements UserDetails {

    @Getter
    private final Cuenta cuenta;

    public CuentaUserDetails(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + cuenta.getRol().getNombre()));
    }

    @Override
    public String getPassword() {
        return cuenta.getPassword();
    }

    @Override
    public String getUsername() {
        return cuenta.getUsuario();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "activo".equals(cuenta.getEstado());
    }

    public Integer getIdCuenta() {
        return cuenta.getIdCuenta();
    }
}
