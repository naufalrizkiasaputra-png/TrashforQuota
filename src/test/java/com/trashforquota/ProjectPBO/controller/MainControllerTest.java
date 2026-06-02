package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import com.trashforquota.ProjectPBO.controller.MainController;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
public class MainControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    private MainController mainController;

    @BeforeEach
    void setUp() {
        mainController = new MainController(userService);
    }

    // --- TEST PERTAMA: SUKSES PROSES REGISTRASI USER ---
    @Test
    public void testFirst_ProcessRegisterSukses() {
        // 1. Arrange (Siapkan data input form)
        String username = "hafidz_alghazali";
        String password = "passwordFidz123";
        String nomorHp = "08123456789";

        // 2. Act (Jalankan fungsi register)
        String viewName = mainController.processRegister(username, password, nomorHp, redirectAttributes);

        // 3. Assert
        // Memastikan dialihkan kembali ke halaman login setelah daftar
        assertEquals("redirect:/login", viewName);

        // Memastikan flash attribute sukses terkirim ke UI
        verify(redirectAttributes, times(1)).addFlashAttribute("successMsg", "Registrasi berhasil! Silakan login.");

        // Menangkap objek user yang dibuat di dalam method untuk dicek valilasinya
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).registerUser(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(username, savedUser.getUsername());
        assertEquals(password, savedUser.getPassword());
        assertEquals(nomorHp, savedUser.getNomorHp());
    }

    // --- TEST KEDUA: SUKSES REDIRECT DASHBOARD KE USER HOME ---
    @Test
    public void testSecond_DashboardRedirectToUserHome() {
        // 1. Arrange (Simulasikan user login yang memiliki authority ROLE_USER)
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();

        // 2. Act (Jalankan method dashboard)
        String viewName = mainController.dashboard(authentication);

        // 3. Assert (Pastikan dilempar ke path /user/home bukan /admin/home)
        assertEquals("redirect:/user/home", viewName, "Arah redirect untuk user biasa salah!");
    }
}