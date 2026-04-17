package com.example.certmanager.security;

import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.User;
import com.example.certmanager.repository.GroupMemberRepository;
import com.example.certmanager.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public CustomUserDetailsService(UserRepository userRepository, GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        for (GroupMember membership : memberships) {
            String role = membership.getRole().name();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return new CustomUserDetails(user, authorities);
    }
}
