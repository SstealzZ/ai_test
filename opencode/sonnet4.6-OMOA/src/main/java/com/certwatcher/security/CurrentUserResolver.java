package com.certwatcher.security;

import com.certwatcher.domain.AppUser;
import com.certwatcher.repository.AppUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final AppUserRepository userRepository;

    public CurrentUserResolver(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser resolve() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB: " + username));
    }
}
