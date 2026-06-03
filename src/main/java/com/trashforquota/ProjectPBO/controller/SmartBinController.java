package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.service.SmartBinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/smart-bins")
public class SmartBinController {

    private final SmartBinService smartBinService;

    public SmartBinController(SmartBinService smartBinService) {
        this.smartBinService = smartBinService;
    }

    @GetMapping
    public String smartbinPage(Model model) {
        try {
            List<SmartBin> listBin = smartBinService.getAll();
            
            // PERBAIKAN: Proteksi anti-null saat validasi status otomatis sebelum render HTML
            for (SmartBin bin : listBin) {
                double totalSampah = bin.getTotalSampah();
                double kapasitas = bin.getKapasitas();

                if (kapasitas > 0 && totalSampah >= kapasitas) {
                    bin.setStatus("FULL");
                } else if (!"OFFLINE".equalsIgnoreCase(bin.getStatus())) {
                    bin.setStatus("ACTIVE");
                }
                smartBinService.save(bin); // Perbarui status aman di database
            }

            model.addAttribute("bins", listBin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("smartBin", new SmartBin());
        return "admin/smart-bin";
    }

    @PostMapping("/save")
    public String saveSmartBin(@ModelAttribute SmartBin smartBin, RedirectAttributes redirectAttributes) {
        try {
            // Paksa total sampah baru selalu dimulai dari 0.0 Kg
            smartBin.setTotalSampah(0.0);
            
            // PERBAIKAN: Proteksi anti-null pada properti kapasitas input baru
            Double kapasitas = smartBin.getKapasitas();
            if (kapasitas == null || kapasitas <= 0) {
                smartBin.setStatus("FULL");
            } else if (!"OFFLINE".equalsIgnoreCase(smartBin.getStatus())) {
                smartBin.setStatus("ACTIVE");
            }

            smartBinService.save(smartBin);
            redirectAttributes.addFlashAttribute("message", "SmartBin berhasil disimpan!");
            return "redirect:/admin/smart-bins?success=save";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menyimpan: " + e.getMessage());
            return "redirect:/admin/smart-bins";
        }
    }

    @PostMapping("/reset/{id}")
    public String resetSmartBin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // PERBAIKAN: Penambahan validasi ID anti-null untuk mencegah kegagalan stream mapping
            SmartBin bin = smartBinService.getAll().stream()
                    .filter(b -> b.getIdBin() != null && b.getIdBin().equals(id)) 
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("SmartBin tidak ditemukan"));
            
            bin.setTotalSampah(0.0); // Kembalikan isi sampah ke kosong
            bin.setStatus("ACTIVE"); // Setel ulang status menjadi aktif kembali
            
            smartBinService.save(bin);
            redirectAttributes.addFlashAttribute("message", "SmartBin di lokasi " + bin.getLokasi() + " berhasil dikosongkan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengosongkan SmartBin: " + e.getMessage());
        }
        return "redirect:/admin/smart-bins";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            smartBinService.delete(id);
            return "redirect:/admin/smart-bins?success=delete";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus: " + e.getMessage());
            return "redirect:/admin/smart-bins";
        }
    }
}