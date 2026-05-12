package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import com.trashforquota.ProjectPBO.repository.RewardRepository;
import com.trashforquota.ProjectPBO.service.RewardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final RewardService rewardService;

    public UserController(UserRepository userRepository, RewardRepository rewardRepository, RewardService rewardService) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.rewardService = rewardService;
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        model.addAttribute("user", user);
        model.addAttribute("rewards", rewardRepository.findAll());
        return "home";
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        model.addAttribute("user", user);
        return "profil";
    }

// ... (kode sebelumnya)

@PostMapping("/profil/update")
public String updateProfil(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam("username") String newUsername,
                           @RequestParam("noHp") String newNoHp,
                           RedirectAttributes redirectAttributes) {
    try {
        // 1. Ambil data user lama dari database menggunakan username di session saat ini
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database!"));

        // 2. Validasi: Jika ganti username, pastikan nama baru belum dipakai orang lain
        if (!user.getUsername().equals(newUsername)) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username '" + newUsername + "' sudah ada yang punya!");
                return "redirect:/user/profil";
            }
        }

        // 3. Simpan perubahan ke database
        user.setUsername(newUsername);
        user.setNoHp(newNoHp);
        userRepository.save(user);

        // 4. --- FORCE UPDATE SESSION (INTI MASALAH) ---
        // Kita buat token baru dengan username yang baru
        Authentication oldAuth = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                newUsername,           // Pakai username baru sebagai Principal
                oldAuth.getCredentials(), 
                oldAuth.getAuthorities()
        );
        
        // Pasang token baru ke context browser
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        redirectAttributes.addFlashAttribute("message", "Profil berhasil diperbarui!");
        return "redirect:/user/profil";

    } catch (Exception e) {
        // Cetak error di terminal/console IDE kamu agar kita tahu baris mana yang salah
        e.printStackTrace(); 
        redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
        return "redirect:/user/profil";
    }
}
    // ... (kode leaderboard & info)

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        List<User> topUsers = userRepository.findAllByOrderByPoinDesc();
        model.addAttribute("leaders", topUsers);
        return "leaderboard";
    }

    @GetMapping("/info")
    public String info() { return "info"; }
}