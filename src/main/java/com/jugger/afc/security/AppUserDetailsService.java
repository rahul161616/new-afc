package com.jugger.afc.security;

import com.jugger.afc.entity.AppUser;
import com.jugger.afc.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmailAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                appUser.getEmail(),
                appUser.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }
}
