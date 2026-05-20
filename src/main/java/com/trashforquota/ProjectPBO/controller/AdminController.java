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
    private final KuotaInternetRepository kuotaInternetRepository;
    private final PulsaRepository pulsaRepository;

    // Constructor untuk Dependency Injection
    public AdminController(ItemSampahRepository itemSampahRepository, 
                           TransaksiRepository transaksiRepository,
                           SmartBinRepository smartBinRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           KuotaInternetRepository kuotaInternetRepository, 
                           PulsaRepository pulsaRepository) {               
        this.itemSampahRepository = itemSampahRepository;
        this.transaksiRepository = transaksiRepository;
        this.smartBinRepository = smartBinRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kuotaInternetRepository = kuotaInternetRepository;
        this.pulsaRepository = pulsaRepository;
    }

    // ==========================================
    // 1. HALAMAN UTAMA / NAVIGATION GET MAPPING
    // ==========================================

    // DASHBOARD (STATISTIK)
    @GetMapping("/home")
    public String adminHome(Model model) {
        List<User> allUsers = userRepository.findAll();
        List<SmartBin> allBins = smartBinRepository.findAll();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalSampah", allBins.stream().mapToDouble(SmartBin::getTotalSampah).sum());
        model.addAttribute("pendingTransactions", transaksiRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList()));
        
        return "admin/dashboard"; 
    }

    // DAFTAR ADMIN
    @GetMapping("/daftar-admin")
    public String daftarAdmin(Model model) {
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("admins", allUsers.stream()
                .filter(u -> User.Role.ADMIN.equals(u.getRole())).collect(Collectors.toList()));
        return "admin/daftar-admin";
    }

    // ANTRIAN PENUKARAN POIN
    @GetMapping("/penukaran")
    public String antreanPenukaran(Model model) {
        model.addAttribute("pendingTransactions", transaksiRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList()));
        return "admin/penukaran";
    }

    // MONITORING SMART BIN
    @GetMapping("/smart-bin")
    public String smartBinStatus(Model model) {
        model.addAttribute("bins", smartBinRepository.findAll());
        return "admin/smart-bin";
    }

    // KONFIGURASI POIN SAMPAH
    @GetMapping("/konfigurasi")
    public String konfigurasiPoin(Model model) {
        model.addAttribute("items", itemSampahRepository.findAll());
        return "admin/konfigurasi";
    }

    // KELOLA REWARD (KUOTA & PULSA)
    @GetMapping("/reward")
    public String kelolaReward(Model model) {
        model.addAttribute("listKuota", kuotaInternetRepository.findAll());
        model.addAttribute("listPulsa", pulsaRepository.findAll());
        return "admin/reward"; 
    }

    // ==========================================
    // 2. PROSES ACTION (POST / GET REDIRECT)
    // ==========================================

    // TRANSAKSI
    @PostMapping("/transaksi/approve")
    public String approveTransaksi(@RequestParam Long id, @RequestParam String sn) {
        Transaksi trx = transaksiRepository.findById(id).orElseThrow();
        trx.setStatus("SUCCESS");
        trx.setSerialNumber(sn);
        transaksiRepository.save(trx);
        return "redirect:/admin/penukaran?success=approved";
    }

    // MANAJEMEN USER / ADMIN
    @PostMapping("/user/create-admin")
    public String createAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String noHp) {
        User newAdmin = new User();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password)); 
        newAdmin.setNomorHp(noHp);
        newAdmin.setRole(User.Role.ADMIN);
        userRepository.save(newAdmin);
        return "redirect:/admin/daftar-admin?success=created";
    }

    @PostMapping("/user/update-admin")
    public String updateAdmin(@RequestParam Long id, @RequestParam String noHp, @RequestParam(required = false) String password) {
        User admin = userRepository.findById(id).orElseThrow();
        admin.setNomorHp(noHp);
        if (password != null && !password.isEmpty()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(admin);
        return "redirect:/admin/daftar-admin?success=updated";
    }

    @GetMapping("/user/delete-admin/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/daftar-admin?success=deleted";
    }

    // MANAJEMEN ITEM SAMPAH
    @PostMapping("/item/save")
    public String saveItem(@ModelAttribute ItemSampah item) {
        itemSampahRepository.save(item);
        return "redirect:/admin/konfigurasi?success=itemSaved";
    }

    @GetMapping("/item/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemSampahRepository.deleteById(id);
        return "redirect:/admin/konfigurasi?success=itemDeleted";
    }

    // MANAJEMEN REWARD KUOTA (SUDAH DIPERBAIKI SINKRON DENGAN MODEL MODEL BARU)
    @PostMapping("/reward/save-kuota")
    public String saveKuota(@RequestParam String namaReward, 
                            @RequestParam double jumlahGb, 
                            @RequestParam int masaAktifHari, 
                            @RequestParam int poinDibutuhkan) {
        KuotaInternet kuota = new KuotaInternet();
        
        // Properti dari Class Parent (Reward)
        kuota.setNamaReward(namaReward); 
        kuota.setPoinDibutuhkan(poinDibutuhkan);
        
        // Properti khusus dari Class Child (KuotaInternet)
        kuota.setJumlahGb(jumlahGb); 
        kuota.setMasaAktifHari(masaAktifHari);
        
        kuotaInternetRepository.save(kuota);
        return "redirect:/admin/reward?success=kuotaAdded";
    }

    @GetMapping("/reward/delete-kuota/{id}")
    public String deleteKuota(@PathVariable Long id) {
        kuotaInternetRepository.deleteById(id);
        return "redirect:/admin/reward?success=deleted";
    }

    @PostMapping("/reward/save-pulsa")
    public String savePulsa(@RequestParam String namaReward, 
                            @RequestParam int nominal, // <--- Ubah jadi int jika di model tipe datanya int
                            @RequestParam int poinDibutuhkan) {
        Pulsa pulsa = new Pulsa();
        
        pulsa.setNamaReward(namaReward); 
        pulsa.setPoinDibutuhkan(poinDibutuhkan);
        pulsa.setNominalPulsa(nominal); // <--- Garis merah akan hilang
        
        pulsaRepository.save(pulsa);
        return "redirect:/admin/reward?success=pulsaAdded";
    }

    @GetMapping("/reward/delete-pulsa/{id}")
    public String deletePulsa(@PathVariable Long id) {
        pulsaRepository.deleteById(id);
        return "redirect:/admin/reward?success=deleted";
    }
}