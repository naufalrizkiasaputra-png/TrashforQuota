package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    
    // Method baru untuk mengambil antrean khusus setor sampah atau reward berdasarkan statusnya
    List<Transaksi> findByStatusAndJenisTransaksi(String status, String jenisTransaksi);
}