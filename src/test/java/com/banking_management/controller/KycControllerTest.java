package com.banking_management.controller;

import com.banking_management.model.dto.response.KycResponseDto;
import com.banking_management.model.entity.Status;
import com.banking_management.security.filter.JwtAuthenticationFilter;
import com.banking_management.security.jwt.JwtUtils;
import com.banking_management.service.KycService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KycController.class)
@AutoConfigureMockMvc(addFilters = false)
class KycControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private KycService kycService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "customer02", roles = "CUSTOMER")
    void submitKyc_ValidRequest_Returns200() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "idCardFile", "id.jpg", "image/jpeg", "fake-image-content".getBytes());

        KycResponseDto responseDto = KycResponseDto.builder()
                .id(1L)
                .idNumber("079123456789")
                .fullName("Nguyen Van B")
                .status(Status.PENDING)
                .idCardFrontUrl("https://res.cloudinary.com/test/id.jpg")
                .username("customer02")
                .createdAt(LocalDateTime.now())
                .build();

        when(kycService.submitKyc(any(), any(), eq("customer02"))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/kyc/upload")
                        .file(file)
                        .param("idNumber", "079123456789")
                        .param("fullName", "Nguyen Van B")
                        .param("sex", "MALE")
                        .param("address", "Ha Noi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.idNumber").value("079123456789"));
    }

    @Test
    @WithMockUser(username = "customer02", roles = "CUSTOMER")
    void getMyKyc_ProfileExists_Returns200() throws Exception {
        // Arrange
        KycResponseDto responseDto = KycResponseDto.builder()
                .id(1L)
                .idNumber("079123456789")
                .fullName("Nguyen Van B")
                .status(Status.PENDING)
                .username("customer02")
                .build();

        when(kycService.getMyKyc("customer02")).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/kyc/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van B"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reviewKyc_Confirm_Returns200() throws Exception {
        // Arrange
        KycResponseDto responseDto = KycResponseDto.builder()
                .id(1L)
                .idNumber("079123456789")
                .status(Status.CONFIRM)
                .verifiedAt(LocalDateTime.now())
                .username("customer02")
                .build();

        when(kycService.reviewKyc(eq(1L), eq(Status.CONFIRM))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/v1/staff/kyc/1/review")
                        .param("decision", "CONFIRM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRM"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void reviewKyc_AsCustomer_Returns403() throws Exception {
        // Act & Assert - chỉ ADMIN/STAFF được duyệt
        mockMvc.perform(put("/api/v1/staff/kyc/1/review")
                        .param("decision", "CONFIRM"))
                .andExpect(status().isForbidden());
    }
}