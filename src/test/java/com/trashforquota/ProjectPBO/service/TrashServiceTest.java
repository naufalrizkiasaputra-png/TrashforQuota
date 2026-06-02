package com.trashforquota.ProjectPBO.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.trashforquota.ProjectPBO.model.ItemSampah;
import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.SmartBinRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import com.trashforquota.ProjectPBO.service.TrashService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrashServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmartBinRepository smartBinRepository;

    @Mock
    private TransaksiRepository transaksiRepository;

    private TrashService trashService;

    // Data dummy untuk pengujian
    private User dummyUser;
    private SmartBin dummyBin;
    private ItemSampah dummyItem;

    @BeforeEach
    void setUp() {
        // Inisialisasi service manual dengan repository mock
        trashService = new TrashService(userRepository, transaksiRepository, smartBinRepository);

        // Siapkan objek data dummy awal sebelum setiap test dijalankan
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");
        dummyUser.setPoin(100); // Saldo poin awal user

        dummyBin = new SmartBin();
        dummyBin.setIdBin(99L);
        dummyBin.setLokasi("Gedung Kuliah IT");
        dummyBin.setTotalSampah(10.0); // Berat awal sampah di dalam bin (dalam kg/gram konsisten)

        dummyItem = new ItemSampah();
        dummyItem.setIdItem(5L);
        dummyItem.setJenis("Botol Plastik Pet");
        dummyItem.setNilaiPoinPerGram(2); // 1 gram = 2 poin
    }

    @Test
    public void testSetorSampah_Sukses() {
        // --- 1. ARRANGE (Manipulasi perilaku Mock Repository) ---
        Long userId = 1L;
        Long binId = 99L;
        double beratDisetor = 500.0; // User menyetor 500 gram

        // Ketika repository mencari ID, kembalikan objek dummy yang sudah kita set di atas
        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(smartBinRepository.findById(binId)).thenReturn(Optional.of(dummyBin));

        // --- 2. ACT (Jalankan method yang ingin dites) ---
        trashService.setorSampah(userId, binId, dummyItem, beratDisetor);

        // --- 3. ASSERT (Verifikasi hasil kalkulasi dan logika bisnis) ---
        
        // Perhitungan: poinDidapat = 500g * 2 poin/g = 1000 poin. 
        // Total poin user baru: Poin awal (100) + 1000 = 1100 poin.
        assertEquals(1100, dummyUser.getPoin(), "Kalkulasi update saldo poin user salah!");

        // Total muatan sampah di bin baru: Muatan awal (10.0) + 500.0 = 510.0
        assertEquals(510.0, dummyBin.getTotalSampah(), "Update penambahan beban sampah di SmartBin salah!");

        // Memastikan method save() dipanggil di masing-masing repository database
        verify(userRepository, times(1)).save(dummyUser);
        verify(smartBinRepository, times(1)).save(dummyBin);

        // Menangkap objek transaksi yang dibuat di dalam method untuk dicek isinya
        ArgumentCaptor<Transaksi> transaksiCaptor = ArgumentCaptor.forClass(Transaksi.class);
        verify(transaksiRepository, times(1)).save(transaksiCaptor.capture());
        
        Transaksi transaksiTercatat = transaksiCaptor.getValue();
        assertNotNull(transaksiTercatat, "Objek transaksi tidak berhasil dibuat!");
        assertEquals("SETOR_SAMPAH", transaksiTercatat.getJenisTransaksi());
        assertEquals(1000, transaksiTercatat.getJumlahPoin(), "Poin tercatat di histori transaksi salah!");
        assertEquals(500.0, transaksiTercatat.getBerat());
        assertEquals("Setor Botol Plastik Pet seberat 500.0g", transaksiTercatat.getDetail());
    }
}