package com.trashforquota.ProjectPBO.repository;

import com.trashforquota.ProjectPBO.model.ConfigSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigSettingRepository
        extends JpaRepository<ConfigSetting, Long> {
}