package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.KuotaInternetRepository;
import com.trashforquota.ProjectPBO.repository.PulsaRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
public class TukarControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransaksiRepository transaksiRepository;

    // Tambahkan Mock untuk Repository Baru
    @Mock
    private KuotaInternetRepository kuotaInternetRepository;

    @Mock
    private PulsaRepository pulsaRepository;

    @Mock
    private Principal principal;

    private TukarController tukarController;
    private User dummyUser;
    private Model model;

    @BeforeEach
    void setUp() {
        // Inisialisasi controller secara manual menggunakan objek mock lengkap
        tukarController = new TukarController(
                userRepository, 
                transaksiRepository, 
                kuotaInternetRepository, 
                pulsaRepository
        );
        
        model = new ConcurrentModel(); // Menggunakan implementasi model bawaan Spring untuk testing

        // Siapkan data user dummy
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");
        dummyUser.setNomorHp("089512345678");
        dummyUser.setPoin(1500);
    }

    // --- TEST PERTAMA: SUKSES MENGAKSES HALAMAN TUKAR POIN ---
    @Test
    public void testFirst_TukarPageSukses() {
        // 1. Arrange (Simulasikan auth login session dan response query database)
        when(principal.getName()).thenReturn("hafidz_alghazali");
        when(userRepository.findByUsername("hafidz_alghazali")).thenReturn(Optional.of(dummyUser));
        
        // Simulasikan database reward mengembalikan list kosong agar tidak NullPointerException
        when(kuotaInternetRepository.findAll()).thenReturn(List.of());
        when(pulsaRepository.findAll()).thenReturn(List.of());

        // 2. Act (Jalankan method endpoint GET /tukar)
        String viewName = tukarController.tukarPage(model, principal);

        // 3. Assert (Validasi apakah data terlempar ke view HTML dengan benar)
        
        // Memastikan method mengembalikan string nama template thymeleaf "tukar"
        assertEquals("tukar", viewName, "Nama view template salah!");

        // Memastikan object "user" berhasil disuntikkan ke dalam UI model
        assertEquals(dummyUser, model.getAttribute("user"), "Objek user di model tidak cocok!");

        // Memastikan string "noHp" berhasil ditempelkan ke UI model berdasarkan data entitas user
        assertEquals("089512345678", model.getAttribute("noHp"), "Atribut noHp di model tidak sesuai!");
        
        // Memastikan list reward dikirimkan ke model UI walaupun kosong
        assertEquals(List.of(), model.getAttribute("listKuota"), "Atribut listKuota harus ada di model!");
        assertEquals(List.of(), model.getAttribute("listPulsa"), "Atribut listPulsa harus ada di model!");
    }
}