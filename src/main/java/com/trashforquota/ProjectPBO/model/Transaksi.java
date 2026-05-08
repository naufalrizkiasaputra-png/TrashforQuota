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
    private Long idTransaksi;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "jenis_transaksi")
    private String jenisTransaksi;

    @Column(name = "jumlah_poin")
    private int jumlahPoin;

    private Double berat;
    private String detail;
    
    @Column(insertable = false, updatable = false)
    private LocalDateTime tanggal;
}