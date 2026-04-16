package com.certwatcher.config;

import com.certwatcher.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@Configuration
public class UserDetailsServiceConfig {

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository,
                                                 DataBootstrapper bootstrapper) {
        return username -> userRepository.findByUsername(username)
                .map(appUser -> new User(
                        appUser.getUsername(),
                        bootstrapper.rawPasswordFor(appUser.getUsername()),
                        List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
