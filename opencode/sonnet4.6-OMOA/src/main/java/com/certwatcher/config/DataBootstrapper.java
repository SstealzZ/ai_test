package com.certwatcher.config;

import com.certwatcher.domain.AppUser;
import com.certwatcher.domain.CertGroup;
import com.certwatcher.domain.UserRole;
import com.certwatcher.repository.AppUserRepository;
import com.certwatcher.repository.CertGroupRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Seeds in-memory H2 with two groups and four demo users on startup.
 *
 * Groups:   ops, dev
 * Users:
 *   alice  / password  → ops  / CERT_ADMIN
 *   bob    / password  → ops  / CERT_VIEWER
 *   carol  / password  → dev  / CERT_ADMIN
 *   dave   / password  → dev  / CERT_VIEWER
 *
 * rawPasswords is kept in memory so the UserDetailsService can serve
 * the BCrypt-hashed form-login credentials without a separate password store.
 */
@Component
public class DataBootstrapper {

    private final CertGroupRepository groupRepo;
    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    private final Map<String, String> rawPasswords = new LinkedHashMap<>();

    public DataBootstrapper(CertGroupRepository groupRepo,
                            AppUserRepository userRepo,
                            PasswordEncoder encoder) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostConstruct
    public void seed() {
        CertGroup ops = groupRepo.save(new CertGroup("ops"));
        CertGroup dev = groupRepo.save(new CertGroup("dev"));

        createUser("alice", "password", UserRole.CERT_ADMIN,  ops);
        createUser("bob",   "password", UserRole.CERT_VIEWER, ops);
        createUser("carol", "password", UserRole.CERT_ADMIN,  dev);
        createUser("dave",  "password", UserRole.CERT_VIEWER, dev);
    }

    public String rawPasswordFor(String username) {
        return rawPasswords.getOrDefault(username, "");
    }

    private void createUser(String username, String raw, UserRole role, CertGroup group) {
        rawPasswords.put(username, encoder.encode(raw));
        AppUser user = new AppUser(username, role, group);
        userRepo.save(user);
    }
}
