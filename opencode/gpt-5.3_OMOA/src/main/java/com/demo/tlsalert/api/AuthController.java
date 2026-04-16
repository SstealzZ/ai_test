package com.demo.tlsalert.api;

import com.demo.tlsalert.api.dto.CurrentUserResponse;
import com.demo.tlsalert.security.CurrentUserService;
import com.demo.tlsalert.security.DemoUserPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final CurrentUserService currentUserService;

    public AuthController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        DemoUserPrincipal user = currentUserService.getRequiredUser();
        return new CurrentUserResponse(
            user.getUsername(),
            user.getGroupName(),
            user.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        );
    }
}
