package com.authapi.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Home page";
    }

    @GetMapping("/store")
    public String store() {
        return "Store page";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/home")
    public String adminHome() {
        return "Admin Home page";
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/client/home")
    public String clientHome() {
        return "Client Home page";
    }

}
