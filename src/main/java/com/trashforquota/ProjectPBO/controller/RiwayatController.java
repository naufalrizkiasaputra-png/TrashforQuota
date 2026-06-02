package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RiwayatController {

    private final TransaksiRepository transaksiRepository;
    private final UserRepository userRepository;

    public RiwayatController(TransaksiRepository transaksiRepository, UserRepository userRepository) {
        this.transaksiRepository = transaksiRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/riwayat")
    public String riwayatPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                // Ambil semua transaksi lalu filter hanya transaksi milik user ini
                List<Transaksi> userTransactions = transaksiRepository.findAll().stream()
                        .filter(transaksi -> user.equals(transaksi.getUser()))
                        .collect(Collectors.toList());
                
                if (userTransactions == null) {
                    userTransactions = new ArrayList<>();
                }

                // Lakukan sorting berdasarkan Tanggal / ID secara terbalik (Terbaru di atas)
                // Ditambahkan nullSafe check agar tidak pecah/eror jika field tanggal masih kosong (null)
                List<Transaksi> sortedTransactions = userTransactions.stream()
                        .sorted(Comparator.comparing(Transaksi::getId, Comparator.reverseOrder())) 
                        .collect(Collectors.toList());

                model.addAttribute("transaksis", sortedTransactions);
                model.addAttribute("user", user); // Menyediakan data user jika template membutuhkan informasi saldo/profil
            } else {
                model.addAttribute("transaksis", new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("transaksis", new ArrayList<>());
        }

        return "riwayat"; // Mengarah ke src/main/resources/templates/riwayat.html
    }
}