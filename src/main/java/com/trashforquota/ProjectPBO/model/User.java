package com.trashforquota.ProjectPBO.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nomor_hp")
    private String nomorHp;

    private int poin;

    // TAMBAHKAN FIELD INI
    private String foto;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN, USER
    }

    public String getNoHp() { return nomorHp; }
    public void setNoHp(String noHp) { this.nomorHp = noHp; }
    public String getRoleName() { return role != null ? role.name() : "USER"; }
}