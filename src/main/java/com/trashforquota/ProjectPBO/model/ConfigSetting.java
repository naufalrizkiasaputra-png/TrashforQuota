package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "config_setting")
@Data
public class ConfigSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int poinOrganik;

    private int poinAnorganik;

    private int poinB3;

    private int minimalTukar;
}