package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaksi")
public class Transaksi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaksi")
    private Long id; // Gunakan 'id' saja agar sinkron dengan controller .findById(id)

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "jenis_transaksi")
    private String jenisTransaksi; // Misal: "PULSA", "KUOTA", atau "SETOR_SAMPAH"

    @Column(name = "jumlah_poin")
    private int jumlahPoin;

    private Double berat;
    private String detail;
    
    // TAMBAHKAN DUA FIELD INI UNTUK ADMIN
    private String status = "PENDING"; // Default status
    private String serialNumber; // Untuk menyimpan SN Pulsa/Kuota

    @Column(name = "tanggal", insertable = false, updatable = false)
    private LocalDateTime tanggal;

    // Hibernate membutuhkan ini jika tanggal otomatis dari DB
    @PrePersist
    protected void onCreate() {
        this.tanggal = LocalDateTime.now();
    }
}