package com.certwatcher.controller;

import com.certwatcher.domain.UserRole;
import com.certwatcher.dto.CertificateResponse;
import com.certwatcher.dto.UpdateThresholdRequest;
import com.certwatcher.security.CurrentUserResolver;
import com.certwatcher.service.CertificateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final CertificateService certService;
    private final CurrentUserResolver currentUser;

    public UiController(CertificateService certService, CurrentUserResolver currentUser) {
        this.certService = certService;
        this.currentUser = currentUser;
    }

    @GetMapping("/certs")
    public String listCerts(Model model) {
        List<CertificateResponse> certs = certService.listForCurrentUser();
        var user = currentUser.resolve();
        var group = certService.groupSettings();

        model.addAttribute("certs", certs);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("groupName", user.getGroup().getName());
        model.addAttribute("isAdmin", user.getRole() == UserRole.CERT_ADMIN);
        model.addAttribute("threshold", group.getAlertThresholdDays());
        model.addAttribute("webhookUrl", group.getWebhookUrl() != null ? group.getWebhookUrl() : "");
        return "certs";
    }

    @PostMapping("/certs/upload")
    public String uploadCert(@RequestParam("alias") String alias,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            certService.addFromFile(alias, file);
            redirectAttributes.addFlashAttribute("success", "Certificate '" + alias + "' added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/certs";
    }

    @PostMapping("/certs/from-url")
    public String addFromUrl(@RequestParam("alias") String alias,
                             @RequestParam("hostname") String hostname,
                             @RequestParam(value = "port", defaultValue = "443") int port,
                             RedirectAttributes redirectAttributes) {
        try {
            certService.addFromUrl(alias, hostname, port);
            redirectAttributes.addFlashAttribute("success", "Certificate from '" + hostname + "' added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/certs";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam("alertThresholdDays") int days,
                                 @RequestParam(value = "webhookUrl", required = false) String webhookUrl,
                                 RedirectAttributes redirectAttributes) {
        try {
            certService.updateGroupSettings(new UpdateThresholdRequest(days, webhookUrl));
            redirectAttributes.addFlashAttribute("success", "Settings updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/certs";
    }
}
