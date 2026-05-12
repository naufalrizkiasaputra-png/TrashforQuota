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
        List<SmartBin> allBins = smartBinRepository.findAll();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalSampah", allBins.stream().mapToDouble(SmartBin::getTotalSampah).sum());
        
        model.addAttribute("admins", allUsers.stream()
                .filter(u -> User.Role.ADMIN.equals(u.getRole())).collect(Collectors.toList()));
        model.addAttribute("allMembers", allUsers.stream()
                .filter(u -> User.Role.USER.equals(u.getRole())).collect(Collectors.toList()));
        model.addAttribute("items", itemSampahRepository.findAll());
        model.addAttribute("bins", allBins);
        model.addAttribute("pendingTransactions", transaksiRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList()));
        
        return "admin/dashboard"; 
    }

    @PostMapping("/transaksi/approve")
    public String approveTransaksi(@RequestParam Long id, @RequestParam String sn) {
        Transaksi trx = transaksiRepository.findById(id).orElseThrow();
        trx.setStatus("SUCCESS");
        trx.setSerialNumber(sn);
        transaksiRepository.save(trx);
        return "redirect:/admin/home?success=approved";
    }

    @PostMapping("/user/create-admin")
    public String createAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String noHp) {
        User newAdmin = new User();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password)); 
        newAdmin.setNomorHp(noHp);
        newAdmin.setRole(User.Role.ADMIN);
        userRepository.save(newAdmin);
        return "redirect:/admin/home?success=created";
    }

    // Method untuk memproses update data admin
    @PostMapping("/user/update-admin")
    public String updateAdmin(@RequestParam Long id, 
                            @RequestParam String noHp, 
                            @RequestParam(required = false) String password) {
        User admin = userRepository.findById(id).orElseThrow();
        admin.setNomorHp(noHp);
        
        // Update password hanya jika diisi di form
        if (password != null && !password.isEmpty()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        
        userRepository.save(admin);
        return "redirect:/admin/home?success=adminUpdated";
    }

    @GetMapping("/item/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemSampahRepository.deleteById(id);
        return "redirect:/admin/home";
    }
}