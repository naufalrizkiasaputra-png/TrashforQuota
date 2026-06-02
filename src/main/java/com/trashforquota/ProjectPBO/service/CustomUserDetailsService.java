package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

        String roleName = (user.getRole() != null) ? user.getRole().toString().trim() : "USER";

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roleName.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority(roleName));
            authorities.add(new SimpleGrantedAuthority(roleName.replace("ROLE_", "")));
        } else {
            authorities.add(new SimpleGrantedAuthority(roleName));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}