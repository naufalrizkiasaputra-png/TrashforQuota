package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.ItemSampah;
import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.SmartBinRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrashService {
    private final UserRepository userRepository;
    private final TransaksiRepository transaksiRepository;
    private final SmartBinRepository smartBinRepository;

    public TrashService(UserRepository userRepository, TransaksiRepository transaksiRepository, SmartBinRepository smartBinRepository) {
        this.userRepository = userRepository;
        this.transaksiRepository = transaksiRepository;
        this.smartBinRepository = smartBinRepository;
    }

    @Transactional
    public void setorSampah(Long userId, Long binId, ItemSampah item, double beratGram) {
        User user = userRepository.findById(userId).orElseThrow();
        SmartBin bin = smartBinRepository.findById(binId).orElseThrow();

        // Hitung poin: berat * nilai poin per gram[cite: 1]
        int poinDidapat = (int) (beratGram * item.getNilaiPoinPerGram());

        // Update saldo user & kapasitas bin
        user.setPoin(user.getPoin() + poinDidapat);
        bin.setTotalSampah(bin.getTotalSampah() + beratGram);

        // Catat di tabel transaksi[cite: 1]
        Transaksi t = new Transaksi();
        t.setUser(user);
        t.setJenisTransaksi("SETOR_SAMPAH");
        t.setJumlahPoin(poinDidapat);
        t.setBerat(beratGram);
        t.setDetail("Setor " + item.getJenis() + " seberat " + beratGram + "g");

        userRepository.save(user);
        smartBinRepository.save(bin);
        transaksiRepository.save(t);
    }
}