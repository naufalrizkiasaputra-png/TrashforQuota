package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaksi")
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Menyimpan jumlah poin (bisa poin yang berkurang saat tukar reward, atau poin bertambah saat setor sampah)
    private double jumlahPoin;

    // Status transaksi: "PENDING", "DISETUJUI", "DITOLAK"
    private String status;

    // Pembeda transaksi: "SETOR_SAMPAH" atau "TUKAR_REWARD"
    private String jenisTransaksi;

    // Catatan tambahan (misal: "Penukaran Telkomsel 5GB" atau "Setor Sampah Botol Plastik")
    private String detail;

    // Waktu pencatatan transaksi secara otomatis
    private LocalDateTime tanggal;

    // Relasi opsional ke Reward (bisa bernilai null jika jenisTransaksi adalah "SETOR_SAMPAH")
    @ManyToOne
    @JoinColumn(name = "reward_id", nullable = true)
    private Reward reward;

    // TAMBAHAN BARU: Relasi opsional ke SmartBin (bernilai null jika jenisTransaksi adalah "TUKAR_REWARD")
    @ManyToOne
    @JoinColumn(name = "id_bin", nullable = true)
    private SmartBin smartBin;

    // --- CONSTRUCTOR ---
    public Transaksi() {
        this.tanggal = LocalDateTime.now(); // Set waktu otomatis saat objek dibuat
    }

    // --- GETTER AND SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getJumlahPoin() {
        return jumlahPoin;
    }

    public void setJumlahPoin(double jumlahPoin) {
        this.jumlahPoin = jumlahPoin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJenisTransaksi() {
        return jenisTransaksi;
    }

    public void setJenisTransaksi(String jenisTransaksi) {
        this.jenisTransaksi = jenisTransaksi;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    // TAMBAHAN BARU: Getter dan Setter untuk SmartBin
    public SmartBin getSmartBin() {
        return smartBin;
    }

    public void setSmartBin(SmartBin smartBin) {
        this.smartBin = smartBin;
    }
}