package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reward")
@Inheritance(strategy = InheritanceType.JOINED) // Menentukan strategi Joined Table
public abstract class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reward") // Menyelaraskan dengan kolom PK di MySQL Anda
    private Long idReward;

    @Column(name = "nama_reward")
    private String namaReward;

    @Column(name = "poin_dibutuhkan")
    private Integer poinDibutuhkan;

    @Column(name = "provider")
    private String provider;

    // --- GETTER AND SETTER ---
    public Long getIdReward() { return idReward; }
    public void setIdReward(Long idReward) { this.idReward = idReward; }

    public String getNamaReward() { return namaReward; }
    public void setNamaReward(String namaReward) { this.namaReward = namaReward; }

    public Integer getPoinDibutuhkan() { return poinDibutuhkan; }
    public void setPoinDibutuhkan(Integer poinDibutuhkan) { this.poinDibutuhkan = poinDibutuhkan; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}