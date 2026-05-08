package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.ItemSampah;
import com.trashforquota.ProjectPBO.repository.ItemSampahRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ScanController {

    private final ItemSampahRepository itemSampahRepository;

    // Inject Repository agar bisa ambil data dari database
    public ScanController(ItemSampahRepository itemSampahRepository) {
        this.itemSampahRepository = itemSampahRepository;
    }

    @GetMapping("/scan")
    public String scanPage(Model model) {
        // Kirim daftar item ke halaman scan agar muncul kolom inputnya
        model.addAttribute("items", itemSampahRepository.findAll());
        return "scan"; 
    }

    @PostMapping("/scan")
    public String processScan(HttpServletRequest request, Model model) {
        List<ItemSampah> items = itemSampahRepository.findAll();
        int totalPoin = 0;

        // Ambil nilai berat dari setiap input yang ada di halaman
        for (ItemSampah item : items) {
            String beratInput = request.getParameter("berat_" + item.getIdItem());
            if (beratInput != null && !beratInput.isEmpty()) {
                try {
                    double berat = Double.parseDouble(beratInput);
                    totalPoin += (int) (berat * item.getNilaiPoinPerGram());
                } catch (NumberFormatException e) {
                    // Abaikan jika input bukan angka
                }
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("hasilPoin", totalPoin);
        return "scan";
    }
}