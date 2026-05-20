package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Dipakai Spring Security untuk proses login
    Optional<User> findByUsername(String username);

    // Menampilkan semua user urut poin terbesar ke terkecil
    List<User> findAllByOrderByPoinDesc();

    // 🔥 SOLUSI UTAMA: Menggunakan kueri ber-escape backtick guna menghindari tabrakan Reserved Keyword MySQL
    @Query(value = "SELECT * FROM `users` WHERE role != :role ORDER BY poin DESC", nativeQuery = true)
    List<User> findAllByRoleNotOrderByPoinDesc(@Param("role") String role);
}