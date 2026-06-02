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
    // RUTE BAWAAN: MANAJEMEN KATEGORI SAMPAH
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
    // RUTE BAWAAN: DASHBOARD UTAMA & MONITORING
    // =========================================================================
    @GetMapping("/home")
    public String adminHome(Model model) {
        try {
            List<User> allUsers = userRepository.findAll();
            List<SmartBin> allBins = smartBinRepository.findAll();
            List<Transaksi> allTransactions = transaksiRepository.findAll();

            model.addAttribute("totalUsers", allUsers.size());
            double totalSampah = allBins.stream().mapToDouble(SmartBin::getTotalSampah).sum();
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
    // RUTE AKSI MANAJEMEN: VERIFIKASI SETOR SAMPAH USER
    // =========================================================================
    @PostMapping("/scan/setuju/{id}")
    public String setujuiSampahAdmin(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElse(null);
            if (trx != null && "PENDING".equalsIgnoreCase(trx.getStatus())) {
                User user = trx.getUser();
                
                // Tambahkan poin akumulasi sampah ke dompet user secara permanen
                user.setPoin(user.getPoin() + (int) trx.getJumlahPoin());
                userRepository.save(user);

                // Tandai transaksi telah selesai diverifikasi
                trx.setStatus("DISETUJUI");
                transaksiRepository.save(trx);

                redirectAttributes.addFlashAttribute("message", "Berhasil menyetujui sampah dan menambahkan poin untuk " + user.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/scan/tolak/{id}")
    public String tolakSampahAdmin(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElse(null);
            if (trx != null && "PENDING".equalsIgnoreCase(trx.getStatus())) {
                // Batalkan pengajuan poin tanpa memodifikasi saldo dompet user
                trx.setStatus("DITOLAK");
                transaksiRepository.save(trx);

                redirectAttributes.addFlashAttribute("error", "Pengajuan poin sampah berhasil ditolak.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    // RUTE BAWAAN: MANAJEMEN REWARD (KUOTA & PULSA)
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
            redirectAttributes.addFlashAttribute("success", "kuotaAdded");
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
            redirectAttributes.addFlashAttribute("success", "pulsaAdded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/reward";
    }

    @GetMapping("/reward/delete/{id}")
    public String deleteReward(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            rewardRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "kuotaDeleted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/reward";
    }

    // =========================================================================
    // RUTE BAWAAN: ANTREAN TRANSAKSI USER
    // =========================================================================
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

    @PostMapping("/approve-transaksi")
    public String approveTransaksiDashboard(@RequestParam Long id) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElseThrow();
            trx.setStatus("SUCCESS");
            transaksiRepository.save(trx);
            return "redirect:/admin/home?success=approved";
        } catch (Exception e) {
            return "redirect:/admin/home?error=failedToApprove";
        }
    }

    @PostMapping("/tolak-transaksi")
    public String tolakTransaksiDashboard(@RequestParam Long id) {
        try {
            Transaksi trx = transaksiRepository.findById(id).orElseThrow();
            trx.setStatus("REJECTED");
            User user = trx.getUser();
            if (user != null && trx.getReward() != null) {
                user.setPoin(user.getPoin() + trx.getReward().getPoinDibutuhkan());
                userRepository.save(user);
            }
            transaksiRepository.save(trx);
            return "redirect:/admin/home?success=rejected";
        } catch (Exception e) {
            return "redirect:/admin/home?error=failedToReject";
        }
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