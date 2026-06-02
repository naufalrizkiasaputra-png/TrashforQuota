package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_sampah")
public class ItemSampah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nama; // Menggunakan nama
    private Double poin; // Menggunakan poin

    // Constructor kosong wajib untuk Thymeleaf form
    public ItemSampah() {
    }

    // Getter dan Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public Double getPoin() { return poin; }
    public void setPoin(Double poin) { this.poin = poin; }
}