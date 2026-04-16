package com.demo.tlsalert.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DemoUserStore implements UserDetailsService {

    private final Map<String, DemoUserPrincipal> users;

    public DemoUserStore(PasswordEncoder passwordEncoder) {
        this.users = new HashMap<>();
        users.put("alice", new DemoUserPrincipal(
            "alice",
            passwordEncoder.encode("alice123"),
            "GROUP_A",
            List.of(new SimpleGrantedAuthority("CERT_ADD"), new SimpleGrantedAuthority("CERT_VIEW"))
        ));
        users.put("bob", new DemoUserPrincipal(
            "bob",
            passwordEncoder.encode("bob123"),
            "GROUP_A",
            List.of(new SimpleGrantedAuthority("CERT_VIEW"))
        ));
        users.put("carol", new DemoUserPrincipal(
            "carol",
            passwordEncoder.encode("carol123"),
            "GROUP_B",
            List.of(new SimpleGrantedAuthority("CERT_ADD"), new SimpleGrantedAuthority("CERT_VIEW"))
        ));
        users.put("dave", new DemoUserPrincipal(
            "dave",
            passwordEncoder.encode("dave123"),
            "GROUP_B",
            List.of(new SimpleGrantedAuthority("CERT_VIEW"))
        ));
    }

    @Override
    public DemoUserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        DemoUserPrincipal principal = users.get(username);
        if (principal == null) {
            throw new UsernameNotFoundException("Unknown user: " + username);
        }
        return principal;
    }
}
