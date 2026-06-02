package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.trashforquota.ProjectPBO.controller.AdminController;
import com.trashforquota.ProjectPBO.model.SmartBin;
import com.trashforquota.ProjectPBO.model.Transaksi;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock private ItemSampahRepository itemSampahRepository;
    @Mock private TransaksiRepository transaksiRepository;
    @Mock private SmartBinRepository smartBinRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private KuotaInternetRepository kuotaInternetRepository;
    @Mock private PulsaRepository pulsaRepository;

    private AdminController adminController;
    private Model model;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(
            itemSampahRepository, transaksiRepository, smartBinRepository, 
            userRepository, passwordEncoder, kuotaInternetRepository, pulsaRepository
        );
        model = new ConcurrentModel();
    }

    // --- TEST 1: SUKSES GENERATE STATISTIK DASHBOARD ADMIN (GET) ---
    @Test
    public void testFirst_AdminHomeSukses() {
        // Arrange (Siapkan data dummy untuk kalkulasi stream)
        User u1 = new User();
        User u2 = new User();
        List<User> userList = Arrays.asList(u1, u2); // total 2 users

        SmartBin b1 = new SmartBin(); b1.setTotalSampah(12.5);
        SmartBin b2 = new SmartBin(); b2.setTotalSampah(7.5);
        List<SmartBin> binList = Arrays.asList(b1, b2); // total sampah = 20.0

        Transaksi t1 = new Transaksi(); t1.setStatus("PENDING");
        Transaksi t2 = new Transaksi(); t2.setStatus("SUCCESS");
        List<Transaksi> trxList = Arrays.asList(t1, t2); // pending = 1

        when(userRepository.findAll()).thenReturn(userList);
        when(smartBinRepository.findAll()).thenReturn(binList);
        when(transaksiRepository.findAll()).thenReturn(trxList);

        // Act
        String viewName = adminController.adminHome(model);

        // Assert
        assertEquals("admin/dashboard", viewName);
        assertEquals(2, model.getAttribute("totalUsers"));
        assertEquals(20.0, model.getAttribute("totalSampah"), "Kalkulasi total sampah via stream() salah!");
        
        List<?> pendingResult = (List<?>) model.getAttribute("pendingTransactions");
        assertEquals(1, pendingResult.size(), "Filter status PENDING salah!");
    }

    // --- TEST 2: SUKSES APPROVE TRANSAKSI (POST) ---
    @Test
    public void testSecond_ApproveTransaksiSukses() {
        // Arrange
        Long trxId = 99L;
        String serialNumber = "SN-TELKOMSEL-10GB-XYZ";
        Transaksi dummyTrx = new Transaksi();
        dummyTrx.setId(trxId);
        dummyTrx.setStatus("PENDING");

        when(transaksiRepository.findById(trxId)).thenReturn(Optional.of(dummyTrx));

        // Act
        String viewName = adminController.approveTransaksi(trxId, serialNumber);

        // Assert
        assertEquals("redirect:/admin/penukaran?success=approved", viewName);
        assertEquals("SUCCESS", dummyTrx.getStatus());
        assertEquals(serialNumber, dummyTrx.getSerialNumber());
        verify(transaksiRepository, times(1)).save(dummyTrx);
    }

    // --- TEST 3: SUKSES HAPUS REWARD KUOTA (GET PATH VARIABLE) ---
    @Test
    public void testThird_DeleteKuotaSukses() {
        // Arrange
        Long kuotaId = 5L;

        // Act
        String viewName = adminController.deleteKuota(kuotaId);

        // Assert
        assertEquals("redirect:/admin/reward?success=deleted", viewName);
        verify(kuotaInternetRepository, times(1)).deleteById(kuotaId);
    }
}