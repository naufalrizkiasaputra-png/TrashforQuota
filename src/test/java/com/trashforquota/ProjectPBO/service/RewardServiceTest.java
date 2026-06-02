package com.trashforquota.ProjectPBO.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.trashforquota.ProjectPBO.model.Reward;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.RewardRepository;
import com.trashforquota.ProjectPBO.repository.TransaksiRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import com.trashforquota.ProjectPBO.service.RewardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RewardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private TransaksiRepository transaksiRepository;

    private RewardService rewardService;

    // Data dummy untuk testing
    private User dummyUser;
    private Reward dummyReward;

    @BeforeEach
    void setUp() {
        rewardService = new RewardService(userRepository, rewardRepository, transaksiRepository);

        // Siapkan User dengan saldo poin yang cukup besar
        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");
        dummyUser.setPoin(5000); // Saldo awal: 5000 poin

        // Siapkan Reward dummy (Misal Kuota Internet)
        dummyReward = new Reward();
        dummyReward.setIdReward(10L);
        dummyReward.setNamaReward("Kuota Internet 10 GB");
        dummyReward.setProvider("Telkomsel");
        dummyReward.setPoinDibutuhkan(3000); // Harga reward: 3000 poin
    }

    // --- TEST PERTAMA: SKENARIO SUKSES PENUKARAN REWARD ---
    @Test
    public void testFirst_TukarPoinSukses() {
        // 1. Arrange (Definisikan ID input dan behavior mock database)
        Long userId = 1L;
        Long rewardId = 10L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(dummyReward));

        // 2. Act (Jalankan fungsi penukaran poin)
        String stringResult = rewardService.tukarPoin(userId, rewardId);

        // 3. Assert (Validasi output return string dan efek samping perubahan data)
        
        // Cek apakah pesan return method sudah sesuai ekspektasi string sukses
        assertEquals("Berhasil menukar poin dengan Kuota Internet 10 GB", stringResult);

        // Cek kalkulasi saldo poin: 5000 - 3000 = harus sisa 2000 poin
        assertEquals(2000, dummyUser.getPoin(), "Kalkulasi pemotongan saldo poin user salah!");

        // Pastikan perubahan data user tersimpan ke database
        verify(userRepository, times(1)).save(dummyUser);

        // Tangkap objek transaksi baru yang digenerate otomatis di dalam method
        ArgumentCaptor<Transaksi> transaksiCaptor = ArgumentCaptor.forClass(Transaksi.class);
        verify(transaksiRepository, times(1)).save(transaksiCaptor.capture());

        Transaksi transaksiResult = transaksiCaptor.getValue();
        assertNotNull(transaksiResult, "Objek transaksi tidak tercatat!");
        assertEquals("TUKAR_REWARD", transaksiResult.getJenisTransaksi());
        assertEquals(-3000, transaksiResult.getJumlahPoin(), "Poin transaksi untuk tukar harusnya minus!");
        assertEquals(0.0, transaksiResult.getBerat(), "Reward tidak memiliki berat, harus 0.0");
        assertEquals("Tukar Reward: Kuota Internet 10 GB (Telkomsel)", transaksiResult.getDetail());
    }
}