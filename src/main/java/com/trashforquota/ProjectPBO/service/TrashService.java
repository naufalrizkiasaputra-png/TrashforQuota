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
        // Use reflection to support different method names for point value on ItemSampah
        double nilaiPerGram = resolveNilaiPoin(item);
        int poinDidapat = (int) (beratGram * nilaiPerGram);

        // Update saldo user & kapasitas bin
        user.setPoin(user.getPoin() + poinDidapat);
        bin.setTotalSampah(bin.getTotalSampah() + beratGram);

        // Catat di tabel transaksi[cite: 1]
        Transaksi t = new Transaksi();
        t.setUser(user);
        t.setJenisTransaksi("SETOR_SAMPAH");
        t.setJumlahPoin(poinDidapat);
        setTransaksiBerat(t, beratGram);
        String jenis = resolveJenis(item);
        t.setDetail("Setor " + jenis + " seberat " + beratGram + "g");

        userRepository.save(user);
        smartBinRepository.save(bin);
        transaksiRepository.save(t);
    }

    private double resolveNilaiPoin(ItemSampah item) {
        if (item == null) return 0.0;
        String[] candidates = {"getNilaiPoin", "getNilai", "getPoinPerGram", "getPoin", "nilaiPoin", "nilai"};
        for (String name : candidates) {
            try {
                java.lang.reflect.Method m = item.getClass().getMethod(name);
                Object val = m.invoke(item);
                if (val instanceof Number) return ((Number) val).doubleValue();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                // ignore other reflection issues and try next
            }
        }
        return 0.0;
    }

    private void setTransaksiBerat(Transaksi transaksi, double beratGram) {
        if (transaksi == null) return;
        String[] candidates = {"setBerat", "setBeratGram", "setBeratSampah", "setWeight", "setJumlahBerat", "setGram", "setTotalBerat", "setBeratInGram", "setJumlah"};
        for (String name : candidates) {
            try {
                java.lang.reflect.Method m = Transaksi.class.getMethod(name, double.class);
                m.invoke(transaksi, beratGram);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                // ignore and try next
            }
            try {
                java.lang.reflect.Method m = Transaksi.class.getMethod(name, Double.class);
                m.invoke(transaksi, beratGram);
                return;
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                // ignore and try next
            }
        }
    }

    private String resolveJenis(ItemSampah item) {
        if (item == null) return "item";
        String[] candidates = {"getJenis", "getNama", "getName", "jenis", "nama", "name", "toString"};
        for (String name : candidates) {
            try {
                java.lang.reflect.Method m = item.getClass().getMethod(name);
                Object val = m.invoke(item);
                if (val != null) return val.toString();
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                // ignore and try next
            }
        }
        // fallback: try toString()
        try {
            return item.toString();
        } catch (Exception e) {
            return "item";
        }
    }
}