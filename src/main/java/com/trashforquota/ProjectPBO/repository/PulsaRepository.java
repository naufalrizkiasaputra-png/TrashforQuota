package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.Pulsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PulsaRepository extends JpaRepository<Pulsa, Long> {
}