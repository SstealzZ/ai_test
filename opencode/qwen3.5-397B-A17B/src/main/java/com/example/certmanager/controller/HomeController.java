package com.example.certmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/certificates")
    public String certificatesPage() {
        return "certificates/list";
    }

    @GetMapping("/certificates/new")
    public String newCertificatePage() {
        return "certificates/form";
    }

    @GetMapping("/certificates/{id}")
    public String certificateDetailPage(@PathVariable Long id) {
        return "certificates/detail";
    }

    @GetMapping("/alerts")
    public String alertsPage() {
        return "alerts/list";
    }

    @GetMapping("/groups")
    public String groupsPage() {
        return "groups/list";
    }
}
