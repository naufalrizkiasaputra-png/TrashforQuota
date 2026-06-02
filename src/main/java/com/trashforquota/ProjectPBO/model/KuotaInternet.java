package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kuota_internet")
@PrimaryKeyJoinColumn(name = "id_reward") // Kunci penghubung Foreign Key ke tabel induk
public class KuotaInternet extends Reward {

    @Column(name = "jumlah_gb")
    private Double jumlahGb;

    @Column(name = "masa_aktif_hari")
    private Integer masaAktifHari;

    // --- GETTER AND SETTER ---
    public Double getJumlahGb() { return jumlahGb; }
    public void setJumlahGb(Double jumlahGb) { this.jumlahGb = jumlahGb; }

    public Integer getMasaAktifHari() { return masaAktifHari; }
    public void setMasaAktifHari(Integer masaAktifHari) { this.masaAktifHari = masaAktifHari; }
}