package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.*;
import com.trashforquota.ProjectPBO.repository.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class TukarController {

    private final UserRepository userRepository;
    private final TransaksiRepository transaksiRepository;
    private final KuotaInternetRepository kuotaInternetRepository;
    private final PulsaRepository pulsaRepository;

    // Constructor untuk Dependency Injection
    public TukarController(
            UserRepository userRepository,
            TransaksiRepository transaksiRepository,
            KuotaInternetRepository kuotaInternetRepository,
            PulsaRepository pulsaRepository
    ) {
        this.userRepository = userRepository;
        this.transaksiRepository = transaksiRepository;
        this.kuotaInternetRepository = kuotaInternetRepository;
        this.pulsaRepository = pulsaRepository;
    }

    // ==========================================
    // 1. HALAMAN TUKAR POIN (SISI USER) - AMAN NYAMAN
    // ==========================================
    @GetMapping("/tukar")
    public String tukarPage(Model model, Principal principal) {
        
        // Proteksi awal jika session principal kosong atau null untuk mencegah Error 500
        if (principal == null) {
            System.out.println("=== LOG WARNING: Principal null, mengalihkan ke login ===");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            model.addAttribute("noHp", user.getNomorHp());
            model.addAttribute("user", user);
            
            // Atribut tambahan agar sinkron dengan berbagai kemungkinan pemanggilan di tukar.html
            model.addAttribute("userPoin", user.getPoin());
            model.addAttribute("username", user.getUsername());
        } else {
            // Fallback safety data
            model.addAttribute("userPoin", 0);
            model.addAttribute("username", "Guest");
        }

        try {
            // AMBIL DATA DARI DATABASE SECARA DINAMIS
            List<KuotaInternet> listKuota = kuotaInternetRepository.findAll();
            List<Pulsa> listPulsa = pulsaRepository.findAll();

            // OPER DATA KE VIEW TIMELINE / TEMPLATE USER
            model.addAttribute("listKuota", listKuota);
            model.addAttribute("listPulsa", listPulsa);
            
        } catch (Exception e) {
            System.err.println("=== LOG CRITICAL: Gagal mengambil list data reward dari database ===");
            e.printStackTrace();
            // Kirim list kosong agar mesin parser Thymeleaf tidak error/crash
            model.addAttribute("listKuota", Collections.emptyList());
            model.addAttribute("listPulsa", Collections.emptyList());
        }

        return "tukar"; // Mengarah ke file src/main/resources/templates/tukar.html
    }

    // ==========================================
    // 2. PROSES TUKAR REWARD (KUOTA & PULSA)
    // ==========================================
    @PostMapping("/tukar")
    public String prosesTukar(
            @RequestParam("nohp") String nohp,
            @RequestParam("rewardId") Long rewardId,
            @RequestParam("jenisReward") String jenisReward, // Nilai wajib: "KUOTA" atau "PULSA"
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            int poinProduk = 0;
            String detailProduk = "";

            try {
                // CARI DATA REWARD SEBENARNYA DARI DATABASE BERDASARKAN ID
                if ("KUOTA".equalsIgnoreCase(jenisReward)) {
                    KuotaInternet kuota = kuotaInternetRepository.findById(rewardId).orElse(null);
                    if (kuota != null) {
                        poinProduk = kuota.getPoinDibutuhkan();
                        detailProduk = kuota.getNamaReward() + " " + kuota.getJumlahGb() + " GB (" + kuota.getMasaAktifHari() + " Hari)";
                    }
                } else if ("PULSA".equalsIgnoreCase(jenisReward)) {
                    Pulsa pulsa = pulsaRepository.findById(rewardId).orElse(null);
                    if (pulsa != null) {
                        poinProduk = pulsa.getPoinDibutuhkan();
                        detailProduk = pulsa.getNamaReward() + " Rp " + pulsa.getNominalPulsa();
                    }
                }
            } catch (Exception e) {
                System.err.println("=== LOG ERROR: Gagal mencari detail ID reward di database ===");
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Terjadi kegagalan system database saat memproses reward! 😢");
                return "redirect:/tukar";
            }

            // Validasi jika produk tidak ditemukan atau ID salah
            if (poinProduk == 0) {
                redirectAttributes.addFlashAttribute("error", "Reward tidak ditemukan atau tidak valid! 😢");
                return "redirect:/tukar";
            }

            // =========================
            // VALIDASI & POTONG POIN USER
            // =========================
            if (user.getPoin() >= poinProduk) {
                // POTONG POIN USER
                user.setPoin(user.getPoin() - poinProduk);
                
                // UPDATE NOMOR HP JIKA BERUBAH
                user.setNomorHp(nohp);
                userRepository.save(user);

                // SIMPAN TRANSAKSI KE DATABASE (Status PENDING agar divalidasi admin di backend)
                Transaksi transaksi = new Transaksi();
                transaksi.setUser(user);
                transaksi.setJenisTransaksi("TUKAR_REWARD");
                transaksi.setJumlahPoin(poinProduk);
                transaksi.setDetail(detailProduk);
                transaksi.setStatus("PENDING"); // Masuk ke antrean persetujuan dashboard admin

                transaksiRepository.save(transaksi);

                redirectAttributes.addFlashAttribute("success", "Permintaan penukaran " + detailProduk + " berhasil dikirim! Menunggu konfirmasi admin. 🎉");
            } else {
                redirectAttributes.addFlashAttribute("error", "Poin kamu tidak cukup untuk menukar " + detailProduk + " 😢");
            }
        }

        return "redirect:/tukar";
    }
}