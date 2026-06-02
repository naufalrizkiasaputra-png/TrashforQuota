package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.trashforquota.ProjectPBO.controller.RiwayatController;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
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
public class RiwayatControllerTest {

    @Mock
    private TransaksiRepository transaksiRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    private RiwayatController riwayatController;
    private User dummyUser;
    private Model model;
    private List<Transaksi> dummyTransaksiList;

    @BeforeEach
    void setUp() {
        riwayatController = new RiwayatController(transaksiRepository, userRepository);
        model = new ConcurrentModel();

        // Siapkan data user dummy
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");

        // Siapkan list transaksi dummy kosong (bisa menampung riwayat data)
        dummyTransaksiList = new ArrayList<>();
        
        Transaksi t1 = new Transaksi();
        t1.setId(101L);
        t1.setJenisTransaksi("SETOR_SAMPAH");
        t1.setJumlahPoin(500);
        dummyTransaksiList.add(t1);
    }

    // --- TEST PERTAMA: SUKSES MENAMPILKAN HALAMAN RIWAYAT TRANSAKSI ---
    @Test
    public void testFirst_RiwayatPageSukses() {
        // 1. Arrange (Mocking session nama user login dan data return query repository)
        when(principal.getName()).thenReturn("hafidz_alghazali");
        when(userRepository.findByUsername("hafidz_alghazali")).thenReturn(Optional.of(dummyUser));
        when(transaksiRepository.findByUserOrderByTanggalDesc(dummyUser)).thenReturn(dummyTransaksiList);

        // 2. Act (Eksekusi fungsi endpoint GET /riwayat)
        String viewName = riwayatController.riwayatPage(model, principal);

        // 3. Assert (Validasi penempatan data model untuk dikirim ke template Thymeleaf)
        
        // Memastikan file mengarah ke template UI bernama "riwayat" (.html)
        assertEquals("riwayat", viewName, "Kembalian nama view template salah!");

        // Memastikan model membawa atribut "transaksis" berisi list riwayat yang tepat
        assertEquals(dummyTransaksiList, model.getAttribute("transaksis"), "Daftar transaksis di model tidak sesuai!");
        
        // Memastikan jumlah data riwayat yang dibawa di dalam model adalah 1 item
        List<?> resultList = (List<?>) model.getAttribute("transaksis");
        assertEquals(1, resultList.size(), "Jumlah list data transaksi tidak cocok!");
    }
}