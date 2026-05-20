package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class RiwayatController {

    private final TransaksiRepository transaksiRepository;
    private final UserRepository userRepository;

    public RiwayatController(
            TransaksiRepository transaksiRepository,
            UserRepository userRepository
    ) {

        this.transaksiRepository = transaksiRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/riwayat")
    public String riwayatPage(
            Model model,
            Principal principal
    ) {

        String username = principal.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if(user != null){

            model.addAttribute(
                    "transaksis",

                    transaksiRepository
                            .findByUserOrderByTanggalDesc(user)
            );
        }

        return "riwayat";
    }
}