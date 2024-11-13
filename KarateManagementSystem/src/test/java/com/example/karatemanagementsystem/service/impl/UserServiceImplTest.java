package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(user);

        Optional<User> foundUser = userService.getUserById(1L);

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(null);

        Optional<User> foundUser = userService.getUserById(1L);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        userService.deleteUser(user);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void findUsersByTrainingSession_ShouldReturnUsers() {
        TrainingSession session = new TrainingSession();
        session.setId(1L);
        when(userRepository.findByTrainingSessions(session)).thenReturn(Set.of(user));

        Set<User> users = userService.findUsersByTrainingSession(session);

        assertEquals(1, users.size());
        assertTrue(users.contains(user));
        verify(userRepository, times(1)).findByTrainingSessions(session);
    }
}