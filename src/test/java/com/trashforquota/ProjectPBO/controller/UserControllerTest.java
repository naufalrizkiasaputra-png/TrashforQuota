package com.trashforquota.ProjectPBO.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;
import com.trashforquota.ProjectPBO.controller.UserController;
import com.trashforquota.ProjectPBO.model.User;
import com.trashforquota.ProjectPBO.repository.RewardRepository;
import com.trashforquota.ProjectPBO.repository.UserRepository;
import com.trashforquota.ProjectPBO.service.RewardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private RewardService rewardService;
    @Mock
    private UserDetails userDetails;

    private UserController userController;
    private User dummyUser;
    private Model model;

    @BeforeEach
    void setUp() {
        userController = new UserController(userRepository, rewardRepository, rewardService);
        model = new ConcurrentModel();

        dummyUser = new User();
        dummyUser.setId(1L);
        dummyUser.setUsername("hafidz_alghazali");
    }

    @Test
    public void testFirst_GetProfilSukses() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("hafidz_alghazali");
        when(userRepository.findByUsername("hafidz_alghazali")).thenReturn(Optional.of(dummyUser));

        // Act
        String viewName = userController.profil(userDetails, model);

        // Assert
        assertEquals("profil", viewName);
        assertEquals(dummyUser, model.getAttribute("user"));
    }
}