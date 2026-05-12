package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class TukarController {

    private final UserRepository userRepository;

    public TukarController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // HALAMAN TUKAR
    // =========================
    @GetMapping("/tukar")
    public String tukarPage(Model model,
                            Principal principal) {

        String username = principal.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user != null) {

            model.addAttribute(
                    "noHp",
                    user.getNomorHp()
            );

            model.addAttribute(
                    "user",
                    user
            );
        }

        return "tukar";
    }

    // =========================
    // PROSES TUKAR
    // =========================
    @PostMapping("/tukar")
    public String prosesTukar(

            @RequestParam("nohp") String nohp,
            @RequestParam("produk") String produk,
            Principal principal,
            RedirectAttributes redirectAttributes

    ) {

        String username = principal.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user != null) {

            int poinProduk = 0;

            try {

                // contoh:
                // "10 GB - 400 poin"

                String angka =
                        produk.replaceAll("[^0-9]", " ");

                String[] parts =
                        angka.trim().split("\\s+");

                poinProduk =
                        Integer.parseInt(
                                parts[parts.length - 1]
                        );

            } catch (Exception e) {

                e.printStackTrace();
            }

            // =========================
            // CEK POIN
            // =========================
            if (user.getPoin() >= poinProduk) {

                // POTONG POIN
                user.setPoin(
                        user.getPoin() - poinProduk
                );

                // UPDATE NOMOR HP
                user.setNomorHp(nohp);

                // SAVE DATABASE
                userRepository.save(user);

                // POPUP SUCCESS
                redirectAttributes.addFlashAttribute(
                        "success",
                        "Reward berhasil ditukarkan 🎉"
                );

            } else {

                // POPUP ERROR
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Poin kamu tidak cukup 😢"
                );
            }
        }

        return "redirect:/tukar";
    }
}