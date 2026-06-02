package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.Reward;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.RewardRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardService {
    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final TransaksiRepository transaksiRepository;

    public RewardService(UserRepository userRepository, RewardRepository rewardRepository, TransaksiRepository transaksiRepository) {
        this.userRepository = userRepository;
        this.rewardRepository = rewardRepository;
        this.transaksiRepository = transaksiRepository;
    }

    @Transactional
    public String tukarPoin(Long userId, Long rewardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new RuntimeException("Reward tidak tersedia"));

        // Validasi poin: Sesuaikan dengan nama field 'poin' di Entity User
        if (user.getPoin() < reward.getPoinDibutuhkan()) {
            return "Gagal: Poin tidak cukup. Saldo Anda: " + user.getPoin();
        }

        // Potong poin user
        user.setPoin(user.getPoin() - reward.getPoinDibutuhkan());
        userRepository.save(user);

        // Catat riwayat penukaran di tabel transaksi[cite: 1]
        Transaksi t = new Transaksi();
        t.setUser(user);
        // t.setReward(reward); // Aktifkan jika di Entity Transaksi ada field reward
        t.setJenisTransaksi("TUKAR_REWARD");
        t.setJumlahPoin(-reward.getPoinDibutuhkan()); // Simpan minus untuk penukaran
        // Provider accessor not present on Reward entity; include only reward name
        t.setDetail("Tukar Reward: " + reward.getNamaReward());

        transaksiRepository.save(t);

        return "Berhasil menukar poin dengan " + reward.getNamaReward();
    }
}