package com.trashforquota.ProjectPBO.controller;

import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.service.SmartBinService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/smart-bins")
public class SmartBinController {

    private final SmartBinService smartBinService;

    public SmartBinController(SmartBinService smartBinService) {
        this.smartBinService = smartBinService;
    }

    @GetMapping
    public String smartbinPage(Model model) {

        model.addAttribute("bins", smartBinService.getAll());
        model.addAttribute("smartBin", new SmartBin());

        return "admin/smart-bin";
    }

    @PostMapping("/save")
    public String saveSmartBin(
            @ModelAttribute SmartBin smartBin
    ) {

        smartBinService.save(smartBin);

        return "redirect:/admin/smart-bins?success=save";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id
    ) {

        smartBinService.delete(id);

        return "redirect:/admin/smart-bins?success=delete";
    }
}