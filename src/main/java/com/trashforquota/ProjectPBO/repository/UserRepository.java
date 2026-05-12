package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List; // Import List diperlukan di sini

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Dipakai Spring Security untuk proses login
    Optional<User> findByUsername(String username);

    // Tambahkan ini untuk fitur Leaderboard:
    // Mencari semua user dan mengurutkan berdasarkan field 'poin' secara Descending (besar ke kecil)
    List<User> findAllByOrderByPoinDesc();
}