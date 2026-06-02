package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.*;
import com.trashforquota.ProjectPBO.repository.*;
import com.trashforquota.ProjectPBO.service.RewardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final TransaksiRepository transaksiRepository;
    private final RewardService rewardService;
    private final ItemSampahRepository itemSampahRepository; 

    public UserController(UserRepository userRepository, 
                          RewardRepository rewardRepository, 
                          TransaksiRepository transaksiRepository,
                          RewardService rewardService,
                          ItemSampahRepository itemSampahRepository) { 
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.transaksiRepository = transaksiRepository;
        this.rewardService = rewardService;
        this.itemSampahRepository = itemSampahRepository; 
    }

    // =========================================================================
    // FITUR USER: SCAN SAMPAH PINTAR MANDIRI (Mendapatkan Poin)
    // =========================================================================
    
    @GetMapping("/scan")
    public String showUserScanPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
            model.addAttribute("user", user);

            // Tambahkan list pending ke notifikasi bell halaman scan jika diperlukan
            List<Transaksi> pendingNotifs = transaksiRepository.findAll().stream()
                .filter(t -> t.getUser() != null && t.getUser().equals(user))
                .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
            model.addAttribute("listTransaksiPending", pendingNotifs);

            ScanForm scanForm = new ScanForm();
            scanForm.setJenis(new ArrayList<>(List.of(""))); 
            scanForm.setBerat(new ArrayList<>(List.of(0)));
            
            model.addAttribute("scanForm", scanForm);
            model.addAttribute("items", itemSampahRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "scan"; 
    }

    @PostMapping("/scan")
    public String handleUserScanAction(@AuthenticationPrincipal UserDetails userDetails,
                                       @ModelAttribute("scanForm") ScanForm scanForm, 
                                       @RequestParam(value = "action", required = true) String action,
                                       @RequestParam(value = "index", required = false) Integer index,
                                       Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
            model.addAttribute("user", user);

            if (scanForm.getJenis() == null) scanForm.setJenis(new ArrayList<>());
            if (scanForm.getBerat() == null) scanForm.setBerat(new ArrayList<>());

            switch (action) {
                case "tambah":
                    scanForm.getJenis().add("");
                    scanForm.getBerat().add(0);
                    break;

                case "hapus":
                    if (index != null && scanForm.getJenis().size() > 1) {
                        scanForm.getJenis().remove(index.intValue());
                        scanForm.getBerat().remove(index.intValue());
                    }
                    break;

                case "hitung":
                    double totalPoin = 0.0;
                    List<ItemSampah> daftarSampahDB = itemSampahRepository.findAll();

                    for (int i = 0; i < scanForm.getJenis().size(); i++) {
                        String selectedIdStr = scanForm.getJenis().get(i);
                        Integer beratGram = scanForm.getBerat().get(i);

                        if (selectedIdStr != null && !selectedIdStr.isEmpty() && beratGram != null) {
                            Long selectedId = Long.parseLong(selectedIdStr);
                            
                            double poinPerGram = daftarSampahDB.stream()
                                    .filter(item -> item.getId().equals(selectedId))
                                    .map(ItemSampah::getPoin)
                                    .findFirst()
                                    .orElse(0.0);

                            totalPoin += (poinPerGram * beratGram);
                        }
                    }
                    model.addAttribute("hasilPoin", totalPoin);
                    break;
            }
            
            // Masukkan data notifikasi pendukung update real-time
            List<Transaksi> pendingNotifs = transaksiRepository.findAll().stream()
                .filter(t -> t.getUser() != null && t.getUser().equals(user))
                .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());
            model.addAttribute("listTransaksiPending", pendingNotifs);

            model.addAttribute("scanForm", scanForm);
            model.addAttribute("items", itemSampahRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "scan";
    }

    // ENDPOINT BARU: Memproses pengiriman ajuan poin sampah ke database dengan status PENDING
    @PostMapping("/scan/kirim")
    public String kirimPengajuanSampah(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam("totalPoin") double totalPoin,
                                       RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            // Membuat record Transaksi baru menggunakan model Transaksi yang disesuaikan
            Transaksi trxSampah = new Transaksi();
            trxSampah.setUser(user);
            trxSampah.setJumlahPoin(totalPoin);
            trxSampah.setStatus("PENDING");
            trxSampah.setJenisTransaksi("SETOR_SAMPAH");
            trxSampah.setDetail("Permintaan verifikasi setor sampah sebesar " + totalPoin + " Poin");

            transaksiRepository.save(trxSampah);

            return "redirect:/user/scan?suksesKirim=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/scan";
        }
    }

    // =========================================================================
    // RUTE BAWAAN: DASHBOARD UTAMA USER & LAINNYA
    // =========================================================================
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        model.addAttribute("user", user);
        
        // Mengambil data transaksi berstatus PENDING/Update untuk disalurkan ke Floating Bell
        List<Transaksi> allTransactions = transaksiRepository.findAll();
        List<Transaksi> pendingNotifs = allTransactions.stream()
            .filter(t -> t.getUser() != null && t.getUser().equals(user))
            .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
            .collect(Collectors.toList());
        
        // Menggunakan nama attribute tunggal 'listTransaksiPending' agar singkron dengan template HTML baru
        model.addAttribute("listTransaksiPending", pendingNotifs);
        
        try {
            model.addAttribute("rewards", rewardRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("rewards", new ArrayList<>()); 
        }
        
        return "home"; 
    }

    @GetMapping("/tukar-poin")
    public String tukarPoinPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        model.addAttribute("user", user);

        List<Transaksi> allTransactionsForUser = transaksiRepository.findAll();
        List<Transaksi> userTransactions = allTransactionsForUser.stream()
            .filter(t -> t.getUser() != null && t.getUser().equals(user))
            .collect(Collectors.toList());
        model.addAttribute("userTransactions", userTransactions);

        // Tambahkan juga list transaksi pending di halaman tukar agar Bell Notifikasi di pojok kanan bawah tetap aktif di sini
        List<Transaksi> pendingNotifs = allTransactionsForUser.stream()
            .filter(t -> t.getUser() != null && t.getUser().equals(user))
            .filter(t -> t.getStatus() != null && "PENDING".equalsIgnoreCase(t.getStatus()))
            .collect(Collectors.toList());
        model.addAttribute("listTransaksiPending", pendingNotifs);

        List<Reward> allRewards = rewardRepository.findAll();
        List<Reward> listKuota = allRewards.stream()
                .filter(r -> r.getClass().getSimpleName().toLowerCase().contains("kuota"))
                .collect(Collectors.toList());
        List<Reward> listPulsa = allRewards.stream()
                .filter(r -> r.getClass().getSimpleName().toLowerCase().contains("pulsa"))
                .collect(Collectors.toList());

        model.addAttribute("listKuota", listKuota);
        model.addAttribute("listPulsa", listPulsa);

        return "user/tukar"; 
    }

    @PostMapping("/tukar/proses")
    public String prosesTukarPoin(@AuthenticationPrincipal UserDetails userDetails,
                                  @RequestParam("idReward") Long idReward,
                                  @RequestParam("noHp") String noHp,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            Reward reward = rewardRepository.findById(idReward)
                    .orElseThrow(() -> new RuntimeException("Pilihan reward tidak ditemukan di database!"));

            if (user.getPoin() < reward.getPoinDibutuhkan()) {
                redirectAttributes.addFlashAttribute("error", "Maaf, sisa poin Anda tidak mencukupi untuk reward ini.");
                return "redirect:/user/tukar-poin";
            }

            user.setPoin(user.getPoin() - reward.getPoinDibutuhkan());
            userRepository.save(user);

            Transaksi transaksiBaru = new Transaksi();
            transaksiBaru.setUser(user);
            transaksiBaru.setReward(reward);
            transaksiBaru.setStatus("PENDING"); 
            transaksiBaru.setJenisTransaksi("TUKAR_REWARD");
            transaksiBaru.setDetail(reward.getNamaReward());

            transaksiRepository.save(transaksiBaru);

            redirectAttributes.addFlashAttribute("message", "Sukses! Permintaan penukaran " + reward.getNamaReward() + " sedang menunggu konfirmasi admin.");
            return "redirect:/user/tukar-poin";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan sistem: " + e.getMessage());
            return "redirect:/user/tukar-poin";
        }
    }

    @GetMapping("/profil")
    public String profil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));
        model.addAttribute("user", user);
        return "profil";
    }

    @PostMapping("/profil/update")
    public String updateProfil(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("username") String newUsername,
                               @RequestParam(value = "noHp", required = false) String newNoHp, 
                               HttpServletRequest request, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan di database!"));

            boolean isUsernameChanged = !user.getUsername().equalsIgnoreCase(newUsername);

            if (isUsernameChanged) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Username '" + newUsername + "' sudah ada yang punya!");
                    return "redirect:/user/profil";
                }
            }

            user.setUsername(newUsername);
            user.setNoHp(newNoHp); 
            
            userRepository.save(user);

            if (isUsernameChanged) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                redirectAttributes.addFlashAttribute("message", "Username berhasil diubah! Silakan login kembali.");
                return "redirect:/login?logout"; 
            }

            redirectAttributes.addFlashAttribute("message", "Profil berhasil diperbarui!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace(); 
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @PostMapping("/profil/upload")
    public String uploadFoto(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Silakan pilih foto terlebih dahulu!");
            return "redirect:/user/profil";
        }

        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            String userDir = System.getProperty("user.dir");
            String uploadDir = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            
            if (!userDir.endsWith("TrashforQuota")) {
                File subDir = new File(userDir, "TrashforQuota");
                if (subDir.exists()) {
                    uploadDir = subDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
                }
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (user.getFoto() != null && !user.getFoto().isEmpty()) {
                Path oldFilePath = uploadPath.resolve(user.getFoto());
                Files.deleteIfExists(oldFilePath);
            }

            String extension = ".jpg"; 
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = "user_" + user.getId() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + extension;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setFoto(fileName);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Foto profil berhasil diperbarui!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal mengunggah foto: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @PostMapping("/profil/photo/delete")
    public String deleteFoto(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan!"));

            String userDir = System.getProperty("user.dir");
            String uploadDir = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
            
            if (!userDir.endsWith("TrashforQuota")) {
                File subDir = new File(userDir, "TrashforQuota");
                if (subDir.exists()) {
                    uploadDir = subDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "img" + File.separator + "uploads" + File.separator;
                }
            }

            Path uploadPath = Paths.get(uploadDir);

            if (user.getFoto() != null && !user.getFoto().isEmpty()) {
                Path filePath = uploadPath.resolve(user.getFoto());
                Files.deleteIfExists(filePath);
            }

            user.setFoto(null);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", "Foto profil berhasil dihapus!");
            return "redirect:/user/profil";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus foto: " + e.getMessage());
            return "redirect:/user/profil";
        }
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        try {
            List<User> topUsers = userRepository.findAllByRoleNotOrderByPoinDesc("ADMIN");
            model.addAttribute("leaders", topUsers);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("leaders", new ArrayList<User>());
        }
        return "leaderboard";
    }

    @GetMapping("/info")
    public String info() { 
        return "info"; 
    }

    public static class ScanForm {
        private List<String> jenis;
        private List<Integer> berat;

        public List<String> getJenis() { return jenis; }
        public void setJenis(List<String> jenis) { this.jenis = jenis; }
        public List<Integer> getBerat() { return berat; }
        public void setBerat(List<Integer> berat) { this.berat = berat; }
    }
}