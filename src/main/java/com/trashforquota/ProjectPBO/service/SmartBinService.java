package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.repository.SmartBinRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmartBinService {

    private final SmartBinRepository smartBinRepository;

    public SmartBinService(SmartBinRepository smartBinRepository) {
        this.smartBinRepository = smartBinRepository;
    }

    public List<SmartBin> getAll() {
        return smartBinRepository.findAll();
    }

    public void save(SmartBin smartBin) {
        smartBinRepository.save(smartBin);
    }

    public void delete(Long id) {
        smartBinRepository.deleteById(id);
    }
}