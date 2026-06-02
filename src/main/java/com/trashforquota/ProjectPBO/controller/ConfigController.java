package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.ConfigSetting;
import com.trashforquota.ProjectPBO.service.ConfigSettingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/config")
public class ConfigController {

    private final ConfigSettingService service;

    public ConfigController(ConfigSettingService service) {
        this.service = service;
    }

    @GetMapping
    public String configPage(Model model) {

        model.addAttribute("config",
                service.getConfig());

        return "admin/config";
    }

@PostMapping("/save")
public String save(
        @ModelAttribute ConfigSetting config
) {

    service.save(config);

    return "redirect:/admin/config?success=save";
}
}