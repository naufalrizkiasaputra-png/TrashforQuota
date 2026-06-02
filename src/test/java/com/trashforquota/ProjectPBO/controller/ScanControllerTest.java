package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.trashforquota.ProjectPBO.controller.ScanController;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.ItemSampahRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
public class ScanControllerTest {

    @Mock
    private ItemSampahRepository itemSampahRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransaksiRepository transaksiRepository;

    @Mock
    private Principal principal;

    private ScanController scanController;
    private User dummyUser;
    private Model model;

    @BeforeEach
    void setUp() {
        scanController = new ScanController(itemSampahRepository, userRepository, transaksiRepository);
        model = new ConcurrentModel();

        // Setup user awal dengan saldo 100 poin
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");
        dummyUser.setPoin(100);
    }

    // --- TEST PERTAMA: SUKSES SCAN MULTI-ITEM SAMPAH & HITUNG POIN ---
    @Test
    public void testFirst_ProcessScanSukses() {
        // 1. Arrange (Simulasikan kiriman data form scan)
        List<String> jenisList = Arrays.asList("organik", "anorganik");
        List<Integer> beratList = Arrays.asList(200, 150); 
        // Perhitungan internal: 
        // - organik   = 200 * 1 = 200 poin
        // - anorganik = 150 * 2 = 300 poin
        // Total didapat harusnya = 500 poin. 
        // Poin baru user: 100 (awal) + 500 = 600 poin.

        when(principal.getName()).thenReturn("hafidz_alghazali");
        when(userRepository.findByUsername("hafidz_alghazali")).thenReturn(Optional.of(dummyUser));
        when(itemSampahRepository.findAll()).thenReturn(Arrays.asList()); // Kosongkan list item gak masalah untuk testing view

        // 2. Act
        String viewName = scanController.processScan(jenisList, beratList, model, principal);

        // 3. Assert
        // Pastikan kembali ke view "scan"
        assertEquals("scan", viewName);

        // Validasi apakah total poin yang dihitung dikirim ke HTML dengan benar
        assertEquals(500, model.getAttribute("hasilPoin"), "Kalkulasi total poin switch-case salah!");

        // Validasi update saldo user di object model & database
        assertEquals(600, dummyUser.getPoin(), "Poin user tidak bertambah dengan benar!");
        verify(userRepository, times(1)).save(dummyUser);

        // Verifikasi pembuatan riwayat transaksi audit
        ArgumentCaptor<Transaksi> transaksiCaptor = ArgumentCaptor.forClass(Transaksi.class);
        verify(transaksiRepository, times(1)).save(transaksiCaptor.capture());

        Transaksi transaksiSaved = transaksiCaptor.getValue();
        assertEquals("SETOR_SAMPAH", transaksiSaved.getJenisTransaksi());
        assertEquals(500, transaksiSaved.getJumlahPoin());
        assertEquals("SUCCESS", transaksiSaved.getStatus());
        assertEquals(350.0, transaksiSaved.getBerat(), "Total berat akumulasi stream() salah!");
    }
}