package com.banking_management.service;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.UpdateUserRequest;
import com.banking_management.model.dto.response.UserResponseDto;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.User;
import com.banking_management.repository.RoleRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserResponseDto mockDto;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        mockUser = User.builder()
                .id(1L).username("customer01")
                .email("customer01@bank.com")
                .phoneNumber("0900000003")
                .isActive(true).isKyc(false)
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        mockDto = UserResponseDto.builder()
                .id(1L).username("customer01")
                .email("customer01@bank.com")
                .roleName("CUSTOMER")
                .build();
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsername("customer01")).thenReturn(Optional.of(mockUser));

        // Act
        var result = userService.loadUserByUsername("customer01");

        // Assert
        assertNotNull(result);
        assertEquals("customer01", result.getUsername());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("ghost"));
    }

    @Test
    void getUserById_UserExists_ReturnsDto() {
        // Arrange
        when(userRepository.findUserDtoById(1L)).thenReturn(Optional.of(mockDto));

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("customer01", result.getUsername());
        assertEquals("CUSTOMER", result.getRoleName());
    }

    @Test
    void getUserById_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findUserDtoById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void deleteUser_UserExists_DeletesSuccessfully() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(any());
    }
}