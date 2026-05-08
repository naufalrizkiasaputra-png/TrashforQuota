package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item_sampah")
public class ItemSampah {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    private String jenis;

    @Column(name = "nilai_poin_per_gram")
    private int nilaiPoinPerGram;
}