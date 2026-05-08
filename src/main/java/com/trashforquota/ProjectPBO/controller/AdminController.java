package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.*;
import com.trashforquota.ProjectPBO.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ItemSampahRepository itemSampahRepository;
    private final TransaksiRepository transaksiRepository;
    private final SmartBinRepository smartBinRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ItemSampahRepository itemSampahRepository, 
                           TransaksiRepository transaksiRepository,
                           SmartBinRepository smartBinRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.itemSampahRepository = itemSampahRepository;
        this.transaksiRepository = transaksiRepository;
        this.smartBinRepository = smartBinRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/home")
    public String adminHome(Model model) {
        List<User> allUsers = userRepository.findAll();

        // Filter List khusus Admin untuk tabel
        List<User> adminList = allUsers.stream()
                .filter(u -> User.Role.ADMIN.equals(u.getRole()))
                .collect(Collectors.toList());

        // Statistik
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalSampah", smartBinRepository.findAll().stream().mapToDouble(SmartBin::getTotalSampah).sum());
        
        model.addAttribute("admins", adminList);
        model.addAttribute("items", itemSampahRepository.findAll());
        model.addAttribute("newItem", new ItemSampah());
        
        return "admin/dashboard"; 
    }

    // CREATE ADMIN
    @PostMapping("/user/create-admin")
    public String createAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String noHp) {
        User newAdmin = new User();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password)); 
        newAdmin.setNomorHp(noHp);
        newAdmin.setPoin(0);
        newAdmin.setRole(User.Role.ADMIN);
        userRepository.save(newAdmin);
        return "redirect:/admin/home?success=adminCreated";
    }

    // UPDATE/EDIT ADMIN
    @PostMapping("/user/update-admin")
    public String updateAdmin(@RequestParam Long id, @RequestParam String noHp, @RequestParam(required = false) String password) {
        User admin = userRepository.findById(id).orElseThrow();
        admin.setNomorHp(noHp);
        if (password != null && !password.isEmpty()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(admin);
        return "redirect:/admin/home?success=adminUpdated";
    }

    // DELETE ADMIN
    @GetMapping("/user/delete-admin/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/home?success=adminDeleted";
    }

    // MANAJEMEN ITEM
    @PostMapping("/item/save")
    public String saveItem(@ModelAttribute ItemSampah item) {
        itemSampahRepository.save(item);
        return "redirect:/admin/home";
    }

    @GetMapping("/item/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemSampahRepository.deleteById(id);
        return "redirect:/admin/home";
    }
}