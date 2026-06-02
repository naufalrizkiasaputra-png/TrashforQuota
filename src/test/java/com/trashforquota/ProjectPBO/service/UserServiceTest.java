package com.trashforquota.ProjectPBO.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import com.trashforquota.ProjectPBO.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Inisialisasi UserService menggunakan mock repository dan mock encoder
        userService = new UserService(userRepository, passwordEncoder);
    }

    // --- TEST PERTAMA: SKENARIO SUKSES REGISTRASI USER ---
    @Test
    public void testFirst_RegisterUserSukses() {
        // 1. Arrange (Siapkan input data mentah dari user)
        User inputUser = new User();
        inputUser.setUsername("hafidz_alghazali");
        inputUser.setPassword("rahasia123"); // Password mentah / raw text

        // Mocking behavior: jika passwordEncoder enkripsi "rahasia123", kembalikan hash "encrypted_hash_bcrypted"
        when(passwordEncoder.encode("rahasia123")).thenReturn("encrypted_hash_bcrypted");

        // 2. Act (Jalankan fungsi registrasi)
        userService.registerUser(inputUser);

        // 3. Assert (Validasi apakah aturan default & enkripsi berjalan tepat)
        
        // Memastikan password berubah jadi hash Bcrypt, bukan text mentah lagi
        assertEquals("encrypted_hash_bcrypted", inputUser.getPassword(), "Proses enkripsi password gagal!");

        // Memastikan default role di-set ke USER
        assertEquals(User.Role.USER, inputUser.getRole(), "Default Role harusnya USER!");

        // Memastikan saldo poin awal user baru adalah 0
        assertEquals(0, inputUser.getPoin(), "Poin user baru harusnya dimulai dari 0!");

        // Memastikan repository benar-benar memproses penyimpanan data ke database sebanyak 1 kali
        verify(userRepository, times(1)).save(inputUser);
    }
}