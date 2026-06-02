package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;

@Entity
@Table(name = "pulsa")
@PrimaryKeyJoinColumn(name = "id_reward") // Kunci penghubung Foreign Key ke tabel induk
public class Pulsa extends Reward {

    @Column(name = "nominal")
    private Integer nominal;

    @Column(name = "nominal_pulsa")
    private Integer nominalPulsa;

    // --- GETTER AND SETTER ---
    public Integer getNominal() { return nominal; }
    public void setNominal(Integer nominal) { this.nominal = nominal; }

    public Integer getNominalPulsa() { return nominalPulsa; }
    public void setNominalPulsa(Integer nominalPulsa) { this.nominalPulsa = nominalPulsa; }
}