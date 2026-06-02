package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "smart_bin")
@Data
public class SmartBin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bin")
    private Long idBin;

    private String lokasi;
    private double kapasitas;

    @Column(name = "total_sampah")
    private double totalSampah;

    // TAMBAHKAN FIELD INI
    private String status = "ACTIVE"; 
}

