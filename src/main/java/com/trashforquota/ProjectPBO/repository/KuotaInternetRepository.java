package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.KuotaInternet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KuotaInternetRepository extends JpaRepository<KuotaInternet, Long> {
    // JpaRepository sudah menyediakan fungsi standar seperti findAll(), save(), deleteById(), dll.
}