package com.certalert.tlscert.security;

import com.certalert.tlscert.entity.UserAccount;
import com.certalert.tlscert.repository.UserAccountRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JwtToUserConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAccountRepository userAccountRepository;

    public JwtToUserConverter(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = jwt.getSubject();
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("JWT user not found: " + username));

        SecurityUserDetails details = new SecurityUserDetails(user);
        return new JwtAuthenticationToken(
                jwt,
                details.getAuthorities(),
                username
        ) {
            @Override
            public Object getPrincipal() {
                return details;
            }
        };
    }
}
