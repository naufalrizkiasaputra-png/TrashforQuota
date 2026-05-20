package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.ItemSampah;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.ItemSampahRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class ScanController {

    private final ItemSampahRepository itemSampahRepository;
    private final UserRepository userRepository;
    private final TransaksiRepository transaksiRepository;

    // Constructor Injection
public ScanController(
        ItemSampahRepository itemSampahRepository,
        UserRepository userRepository,
        TransaksiRepository transaksiRepository
) {

    this.itemSampahRepository = itemSampahRepository;
    this.userRepository = userRepository;
    this.transaksiRepository = transaksiRepository;
}

    // =========================
    // HALAMAN SCAN
    // =========================
    @GetMapping("/scan")
    public String scanPage(Model model) {

        model.addAttribute("items", itemSampahRepository.findAll());

        return "scan";
    }

    // =========================
    // HITUNG POIN
    // =========================
    @PostMapping("/scan")
    public String processScan(

            @RequestParam("jenis") List<String> jenisList,
            @RequestParam("berat") List<Integer> beratList,
            Model model,
            Principal principal

    ) {

        int totalPoin = 0;

        // =========================
        // HITUNG TOTAL POIN
        // =========================
        for (int i = 0; i < jenisList.size(); i++) {

            String jenis = jenisList.get(i);
            int berat = beratList.get(i);

            switch (jenis.toLowerCase()) {

                case "organik":
                    totalPoin += berat * 1;
                    break;

                case "anorganik":
                    totalPoin += berat * 2;
                    break;

                case "b3":
                    totalPoin += berat * 3;
                    break;
            }
        }

        // =========================
        // AMBIL USER LOGIN
        // =========================
        String username = principal.getName();

        User user = userRepository.findByUsername(username).orElse(null);

        // =========================
        // TAMBAH POIN USER
        // =========================
        user.setPoin(user.getPoin() + totalPoin);

        // SAVE KE DATABASE
        userRepository.save(user);

        Transaksi transaksi = new Transaksi();

transaksi.setUser(user);

transaksi.setJenisTransaksi("SETOR_SAMPAH");

transaksi.setJumlahPoin(totalPoin);

transaksi.setDetail("Scan sampah berhasil");

transaksi.setStatus("SUCCESS");

transaksi.setBerat((double) beratList.stream()
        .mapToInt(Integer::intValue)
        .sum());

transaksiRepository.save(transaksi);

        // =========================
        // KIRIM DATA KE HTML
        // =========================
        model.addAttribute("hasilPoin", totalPoin);
        model.addAttribute("items", itemSampahRepository.findAll());
        model.addAttribute("user", user);

        return "scan";
    }
}