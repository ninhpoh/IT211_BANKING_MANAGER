package com.banking_management.controller;

import com.banking_management.model.dto.response.UserResponseDto;
import com.banking_management.security.filter.JwtAuthenticationFilter;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtils jwtUtils;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_AsAdmin_Returns200() throws Exception {
        // Arrange
        UserResponseDto dto = UserResponseDto.builder()
                .id(1L).username("customer01")
                .email("customer01@bank.com")
                .roleName("CUSTOMER")
                .createdAt(LocalDateTime.now())
                .build();

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("customer01"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllUsers_AsCustomer_Returns403() throws Exception {
        // Act & Assert - CUSTOMER không có quyền
        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_UserExists_Returns200() throws Exception {
        // Arrange
        UserResponseDto dto = UserResponseDto.builder()
                .id(1L).username("customer01")
                .email("customer01@bank.com")
                .roleName("CUSTOMER")
                .build();

        when(userService.getUserById(1L)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("customer01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AsAdmin_Returns200() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void deleteUser_AsStaff_Returns403() throws Exception {
        // Act & Assert - chỉ ADMIN mới được xóa
        mockMvc.perform(delete("/api/v1/users/2"))
                .andExpect(status().isForbidden());
    }
}