package com.certalert.tlscert.config;

import com.certalert.tlscert.entity.Group;
import com.certalert.tlscert.entity.UserAccount;
import com.certalert.tlscert.repository.GroupRepository;
import com.certalert.tlscert.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(GroupRepository groupRepository,
                               UserAccountRepository userAccountRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (groupRepository.count() > 0) {
                return;
            }

            Group groupA = groupRepository.save(Group.builder().name("Platform-Ops").build());
            Group groupB = groupRepository.save(Group.builder().name("Security-Team").build());

            userAccountRepository.save(UserAccount.builder()
                    .username("alice")
                    .password(passwordEncoder.encode("password"))
                    .email("alice@example.com")
                    .group(groupA)
                    .role(UserAccount.Role.CERT_ADMIN)
                    .build());

            userAccountRepository.save(UserAccount.builder()
                    .username("bob")
                    .password(passwordEncoder.encode("password"))
                    .email("bob@example.com")
                    .group(groupA)
                    .role(UserAccount.Role.CERT_VIEWER)
                    .build());

            userAccountRepository.save(UserAccount.builder()
                    .username("charlie")
                    .password(passwordEncoder.encode("password"))
                    .email("charlie@example.com")
                    .group(groupB)
                    .role(UserAccount.Role.CERT_ADMIN)
                    .build());
        };
    }
}
