package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    // Mencari riwayat transaksi berdasarkan user tertentu
        List<Transaksi>
    findByUserOrderByTanggalDesc(User user);
}