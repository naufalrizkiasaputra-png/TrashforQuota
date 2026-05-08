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
@Builder // Memudahkan pembuatan objek User baru di Controller
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    /**
     * Sesuai struktur DB: nama kolom adalah 'nomor_hp'.
     * Kita gunakan @Column untuk mapping agar di Java tetap bisa menggunakan camelCase.
     */
    @Column(name = "nomor_hp")
    private String nomorHp;

    private int poin;

    /**
     * Sesuai struktur DB: tipe data adalah ENUM('ADMIN', 'USER').
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ADMIN, USER
    }

    /**
     * HELPER METHODS
     * Menambahkan getter/setter dengan nama 'noHp' agar sesuai dengan 
     * parameter @RequestParam di AdminController dan variabel di HTML.
     */
    public String getNoHp() {
        return nomorHp;
    }

    public void setNoHp(String noHp) {
        this.nomorHp = noHp;
    }

    /**
     * Digunakan oleh CustomUserDetailsService untuk Spring Security
     */
    public String getRoleName() {
        return role != null ? role.name() : "USER";
    }
}