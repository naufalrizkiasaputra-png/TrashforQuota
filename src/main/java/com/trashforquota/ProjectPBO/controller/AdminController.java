package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.*;
import com.trashforquota.ProjectPBO.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ItemSampahRepository itemSampahRepository;
    private final TransaksiRepository transaksiRepository;
    private final SmartBinRepository smartBinRepository;
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ItemSampahRepository itemSampahRepository, 
                           TransaksiRepository transaksiRepository,
                           SmartBinRepository smartBinRepository,
                           UserRepository userRepository,
                           RewardRepository rewardRepository,
                           PasswordEncoder passwordEncoder) {               
        this.itemSampahRepository = itemSampahRepository;
        this.transaksiRepository = transaksiRepository;
        this.smartBinRepository = smartBinRepository;
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // MANAJEMEN KATEGORI SAMPAH
    // =========================================================================
    @GetMapping("/sampah")
    public String kelolaSampah(Model model) {
        try {
            model.addAttribute("listSampah", itemSampahRepository.findAll());
            model.addAttribute("itemBaru", new ItemSampah());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("listSampah", new ArrayList<>());
            model.addAttribute("itemBaru", new ItemSampah());
        }
        return "admin/kelola_sampah"; 
    }

    @PostMapping("/sampah/simpan")
    public String simpanSampahKategori(@ModelAttribute("itemBaru") ItemSampah item) {
        try {
            itemSampahRepository.save(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/sampah";
    }

    @GetMapping("/sampah/hapus/{id}")
    public String hapusSampahKategori(@PathVariable("id") Long id) {
        try {
            itemSampahRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/sampah";
    }

    // =========================================================================
    // DASHBOARD UTAMA & MONITORING
    // =========================================================================
    @GetMapping("/home")
    public String adminHome(Model model) {
        try {
            List<User> allUsers = userRepository.findAll();
            List<SmartBin> allBins = smartBinRepository.findAll();
            List<Transaksi> allTransactions = transaksiRepository.findAll();

            model.addAttribute("totalUsers", allUsers.size());
            
            // Proteksi mapToDouble dari nilai null pada totalSampah SmartBin
            double totalSampah = allBins.stream()
                    .mapToDouble(SmartBin::getTotalSampah)
                    .sum();
            model.addAttribute("totalSampah", totalSampah);
            
            // 1. Antrean Reward (Filter transaksi PENDING yang BUKAN SETOR_SAMPAH)
            List<Transaksi> pendingTransactions = allTransactions.stream()
                    .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
                    .filter(t -> !"SETOR_SAMPAH".equalsIgnoreCase(t.getJenisTransaksi()))
                    .collect(Collectors.toList());
            model.addAttribute("pendingTransactions", pendingTransactions);

            // 2. Antrean Setor Sampah (Khusus mengambil PENDING & jenisTransaksi == "SETOR_SAMPAH")
            List<Transaksi> antreanSampah = allTransactions.stream()
                    .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
                    .filter(t -> "SETOR_SAMPAH".equalsIgnoreCase(t.getJenisTransaksi()))
                    .collect(Collectors.toList());
            model.addAttribute("antreanSampah", antreanSampah);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/dashboard"; 
    }

    // =========================================================================
    // VERIFIKASI SETOR SAMPAH USER (DENGAN UPDATE SMARTBIN)
    // =========================================================================
    @PostMapping("/scan/setuju/{id}")
    public String setujuiSampahAdmin(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElse(null);
            if (trx != null && "PENDING".equalsIgnoreCase(trx.getStatus())) {
                User user = trx.getUser();
                
                // Pengecekan Nilai untuk Jumlah Poin (getJumlahPoin() mengembalikan primitif double)
                double poinMentah = trx.getJumlahPoin();
                int poinTambahan = (int) Math.round(poinMentah);
                
                if (user != null) {
                    // user.getPoin() is a primitive int in the model, so it cannot be compared to null
                    int currentPoin = user.getPoin();
                    user.setPoin(currentPoin + poinTambahan);
                    userRepository.save(user);
                }

                SmartBin targetBin = trx.getSmartBin();
                
                // LOGIKA FALLBACK KETIKA RELASI SMARTBIN NULL
                if (targetBin == null && trx.getDetail() != null && trx.getDetail().startsWith("Setoran Sampah ke ")) {
                    try {
                        String detailText = trx.getDetail();
                        String namaLokasi = detailText.replace("Setoran Sampah ke ", ""); 
                        int openParenIndex = namaLokasi.indexOf(" (");
                        if (openParenIndex != -1) {
                            namaLokasi = namaLokasi.substring(0, openParenIndex).trim(); 
                        }
                        
                        String finalNamaLokasi = namaLokasi;
                        targetBin = smartBinRepository.findAll().stream()
                                .filter(b -> b.getLokasi() != null && b.getLokasi().trim().equalsIgnoreCase(finalNamaLokasi))
                                .findFirst()
                                .orElse(null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (targetBin != null) {
                    double beratTambahanKg = (poinMentah / 5.0) / 1000.0;
                    Double totalSampahObj = targetBin.getTotalSampah();
                    double sampahSaatIni = totalSampahObj != null ? totalSampahObj : 0.0;
                    double totalSampahBaru = sampahSaatIni + beratTambahanKg;
                    
                    targetBin.setTotalSampah(totalSampahBaru);
                    
                    if (targetBin.getStatus() == null || targetBin.getStatus().trim().isEmpty()) {
                        targetBin.setStatus("ACTIVE");
                    }
                    
                    Double kapasitasObj = targetBin.getKapasitas();
                    double kapasitas = kapasitasObj != null ? kapasitasObj : 0.0;
                    if (kapasitas > 0 && totalSampahBaru >= kapasitas) {
                        targetBin.setStatus("FULL");
                    }
                    
                    smartBinRepository.save(targetBin);
                }

                trx.setStatus("DISETUJUI");
                transaksiRepository.save(trx);

                redirectAttributes.addFlashAttribute("message", "Berhasil menyetujui sampah, kapasitas SmartBin diperbarui!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal memproses verifikasi: " + e.getMessage());
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/scan/tolak/{id}")
    public String tolakSampahAdmin(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElse(null);
            if (trx != null && "PENDING".equalsIgnoreCase(trx.getStatus())) {
                trx.setStatus("DITOLAK");
                transaksiRepository.save(trx);
                redirectAttributes.addFlashAttribute("message", "Pengajuan poin sampah berhasil ditolak.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal menolak: " + e.getMessage());
        }
        return "redirect:/admin/home";
    }

    @GetMapping("/smart-bin")
    public String smartBinStatus(Model model) {
        try {
            model.addAttribute("bins", smartBinRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/smart-bin";
    }

    @GetMapping("/konfigurasi")
    public String konfigurasiPoin(Model model) {
        try {
            model.addAttribute("items", itemSampahRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/konfigurasi";
    }

    // =========================================================================
    // MANAJEMEN REWARD (KUOTA & PULSA)
    // =========================================================================
    @GetMapping("/reward")
    public String kelolaReward(Model model) {
        try {
            List<Reward> allRewards = rewardRepository.findAll();
            model.addAttribute("listReward", allRewards);
            
            model.addAttribute("listKuota", allRewards.stream()
                    .filter(r -> r instanceof KuotaInternet)
                    .collect(Collectors.toList()));
            model.addAttribute("listPulsa", allRewards.stream()
                    .filter(r -> r instanceof Pulsa)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("listReward", new ArrayList<>());
            model.addAttribute("listKuota", new ArrayList<>());
            model.addAttribute("listPulsa", new ArrayList<>());
        }
        return "admin/reward"; 
    }

    @PostMapping("/reward/save-kuota")
    public String saveKuota(@RequestParam("namaReward") String namaReward,
                            @RequestParam("jumlahGb") Double jumlahGb,
                            @RequestParam("formattingMasaAktif") Integer masaAktifHari,
                            @RequestParam("poinDibutuhkan") Integer poinDibutuhkan,
                            RedirectAttributes redirectAttributes) {
        try {
            KuotaInternet kuota = new KuotaInternet();
            kuota.setNamaReward(namaReward);
            kuota.setPoinDibutuhkan(poinDibutuhkan);
            kuota.setProvider("Kuota " + jumlahGb + " GB (" + masaAktifHari + " Hari)");
            kuota.setJumlahGb(jumlahGb);
            kuota.setMasaAktifHari(masaAktifHari);
            rewardRepository.save(kuota);
            redirectAttributes.addFlashAttribute("message", "Kategori Kuota Internet berhasil ditambahkan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/reward";
    }

    @PostMapping("/reward/save-pulsa")
    public String savePulsa(@RequestParam("namaReward") String namaReward,
                            @RequestParam("nominalPulsa") Integer nominalPulsa,
                            @RequestParam("poinDibutuhkan") Integer poinDibutuhkan,
                            RedirectAttributes redirectAttributes) {
        try {
            Pulsa pulsa = new Pulsa();
            pulsa.setNamaReward(namaReward);
            pulsa.setPoinDibutuhkan(poinDibutuhkan);
            pulsa.setProvider("Pulsa Rp " + nominalPulsa);
            pulsa.setNominalPulsa(nominalPulsa);
            rewardRepository.save(pulsa);
            redirectAttributes.addFlashAttribute("message", "Kategori Pulsa berhasil ditambahkan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/reward";
    }

    @GetMapping("/reward/delete/{id}")
    public String deleteReward(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            rewardRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Kategori Hadiah berhasil dihapus.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/reward";
    }

    // =========================================================================
    // FIX UTAMA: ANTREAN VERIFIKASI TRANSAKSI KUOTA/PULSA DIGITAL DASHBOARD
    // =========================================================================
    @PostMapping("/approve-transaksi")
    public String approveTransaksiDashboard(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Data transaksi tidak ditemukan."));
            
            trx.setStatus("SUCCESS");
            transaksiRepository.save(trx);
            
            redirectAttributes.addFlashAttribute("message", "Sukses menyetujui klaim hadiah! Kuota siap dikirim ke user.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyetujui transaksi: " + e.getMessage());
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/tolak-transaksi")
    public String tolakTransaksiDashboard(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Data transaksi tidak ditemukan."));
            
            trx.setStatus("REJECTED");
            User user = trx.getUser();
            
            // Pengembalian poin secara aman dari null pointer check
            if (user != null && trx.getReward() != null) {
                int poinKembali = trx.getReward().getPoinDibutuhkan() != null ? trx.getReward().getPoinDibutuhkan() : 0;
                int currentPoin = user.getPoin();
                user.setPoin(currentPoin + poinKembali);
                userRepository.save(user);
            }
            transaksiRepository.save(trx);
            redirectAttributes.addFlashAttribute("message", "Permintaan klaim berhasil ditolak dan poin user dikembalikan.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menolak transaksi: " + e.getMessage());
        }
        return "redirect:/admin/home";
    }

    @GetMapping("/penukaran")
    public String antreanPenukaran(Model model) {
        try {
            List<Transaksi> pendingTransactions = transaksiRepository.findAll().stream()
                    .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
                    .filter(t -> !"SETOR_SAMPAH".equalsIgnoreCase(t.getJenisTransaksi()))
                    .collect(Collectors.toList());
            model.addAttribute("pendingTransactions", pendingTransactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/penukaran";
    }

    @PostMapping("/transaksi/approve")
    public String approveTransaksi(@RequestParam Long id, @RequestParam String sn) {
        Transaksi trx = transaksiRepository.findById(id).orElseThrow();
        trx.setStatus("SUCCESS");
        transaksiRepository.save(trx);
        return "redirect:/admin/penukaran?success=approved";
    }

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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/daftar-admin")
    public String daftarAdmin(Model model) {
        try {
            List<User> allUsers = userRepository.findAll();
            List<User> admins = allUsers.stream()
                    .filter(u -> u.getRole() != null && User.Role.ADMIN.equals(u.getRole()))
                    .collect(Collectors.toList());
            model.addAttribute("admins", admins);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/daftar-admin";
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/user/create-admin")
    public String createAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String noHp) {
        User newAdmin = new User();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(password)); 
        newAdmin.setNoHp(noHp); 
        newAdmin.setRole(User.Role.ADMIN);
        userRepository.save(newAdmin);
        return "redirect:/admin/daftar-admin?success=created";
    }
}