package com.trashforquota.ProjectPBO.config;

import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Optional<User> existingSuperAdmin = userRepository.findByUsername("superadmin");

        if (existingSuperAdmin.isEmpty()) {
            User superAdmin = new User();
            superAdmin.setUsername("superadmin");
            superAdmin.setPassword(passwordEncoder.encode("admin123")); 
            superAdmin.setNomorHp("081234567890");
            
            // Menggunakan Enum yang sudah kita tambahkan di model User
            superAdmin.setRole(User.Role.SUPER_ADMIN); 
            
            userRepository.save(superAdmin);
            System.out.println(">>> [SUCCESS] Akun Super Admin berhasil digenerate otomatis! (User: superadmin, Pass: admin123)");
        }
    }
}