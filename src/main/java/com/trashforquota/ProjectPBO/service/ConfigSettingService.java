package com.trashforquota.ProjectPBO.service;

import com.trashforquota.ProjectPBO.model.ConfigSetting;
import com.trashforquota.ProjectPBO.repository.ConfigSettingRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfigSettingService {

    private final ConfigSettingRepository repository;

    public ConfigSettingService(ConfigSettingRepository repository) {
        this.repository = repository;
    }

    public ConfigSetting getConfig() {

        return repository.findAll()
                .stream()
                .findFirst()
                .orElse(new ConfigSetting());
    }

    public void save(ConfigSetting config) {
        repository.save(config);
    }
}