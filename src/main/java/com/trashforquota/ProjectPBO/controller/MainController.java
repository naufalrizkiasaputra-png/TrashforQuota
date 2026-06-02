package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController {

    private final UserService userService;

    public MainController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String username, 
                                  @RequestParam String password, 
                                  @RequestParam String nomorHp,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setNomorHp(nomorHp);
            
            userService.registerUser(user);
            
            redirectAttributes.addFlashAttribute("successMsg", "Registrasi berhasil! Silakan login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Gagal daftar: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";

        // Cek apakah user yang login adalah ADMIN atau SUPER_ADMIN
        boolean hasAdminAccess = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                               a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (hasAdminAccess) {
            return "redirect:/admin/home"; // Keduanya masuk ke dashboard admin yang sama
        }
        
        return "redirect:/user/home"; // Jika bukan admin, lempar ke home user biasa
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}