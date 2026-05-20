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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @PostMapping("/profil/update")
    public String updateProfil(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("username") String newUsername,
                               @RequestParam("noHp") String newNoHp,
                               HttpServletRequest request, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database!"));

            boolean isUsernameChanged = !user.getUsername().equals(newUsername);

            if (isUsernameChanged) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Username '" + newUsername + "' sudah ada yang punya!");
                    return "redirect:/user/profil";
                }
            }

            user.setUsername(newUsername);
            user.setNoHp(newNoHp);
            userRepository.save(user);

            if (isUsernameChanged) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                redirectAttributes.addFlashAttribute("message", "Username berhasil diubah! Silakan login kembali.");
                return "redirect:/login?logout"; 
            }

            redirectAttributes.addFlashAttribute("message", "Profil berhasil diperbarui!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace(); 
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @PostMapping("/profil/upload")
    public String uploadFoto(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Silakan pilih foto terlebih dahulu!");
            return "redirect:/user/profil";
        }

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            String userDir = System.getProperty("user.dir");
            String uploadDir = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            
            if (!new File(uploadDir).exists() && userDir.endsWith("TrashforQuota")) {
                uploadDir = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            } else if (!new File(uploadDir).exists()) {
                uploadDir = userDir + File.separator + "TrashforQuota" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (user.getFoto() != null) {
                Path oldFilePath = uploadPath.resolve(user.getFoto());
                Files.deleteIfExists(oldFilePath);
            }

            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "user_" + user.getId() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setFoto(fileName);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Foto profil berhasil diperbarui!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah foto: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @PostMapping("/profil/photo/delete")
    public String deleteFoto(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            String userDir = System.getProperty("user.dir");
            String uploadDir = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            
            if (!new File(uploadDir).exists() && !userDir.endsWith("TrashforQuota")) {
                uploadDir = userDir + File.separator + "TrashforQuota" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            }

            Path uploadPath = Paths.get(uploadDir);

            if (user.getFoto() != null) {
                Path filePath = uploadPath.resolve(user.getFoto());
                Files.deleteIfExists(filePath);
            }

            user.setFoto(null);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Foto profil berhasil dihapus!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus foto: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        // 🔥 Memanggil native query untuk menyaring role "ADMIN" secara aman
        List<User> topUsers = userRepository.findAllByRoleNotOrderByPoinDesc("ADMIN");
        model.addAttribute("leaders", topUsers);
        return "leaderboard";
    }

    @GetMapping("/info")
    public String info() { 
        return "info"; 
    }
}