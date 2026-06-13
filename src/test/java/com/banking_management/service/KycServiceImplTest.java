package com.banking_management.service;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.KycRequest;
import com.banking_management.model.dto.response.KycResponseDto;
import com.banking_management.model.entity.KycProfile;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.Status;
import com.banking_management.model.entity.User;
import com.banking_management.repository.KycProfileRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.impl.KycServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycServiceImplTest {

    @Mock private KycProfileRepository kycProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private KycServiceImpl kycService;

    private User mockUser;
    private KycProfile mockProfile;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name("CUSTOMER").build();
        mockUser = User.builder().id(1L).username("customer02").role(role).isKyc(false).build();

        mockProfile = KycProfile.builder()
                .id(1L).idNumber("079123456789")
                .fullName("Nguyen Van B")
                .status(Status.PENDING)
                .user(mockUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitKyc_AlreadySubmitted_ThrowsException() {
        // Arrange
        KycRequest request = new KycRequest();
        request.setIdNumber("079123456789");
        request.setFullName("Nguyen Van B");

        MultipartFile file = new MockMultipartFile("idCardFile", "id.jpg", "image/jpeg", "data".getBytes());

        when(userRepository.findByUsername("customer02")).thenReturn(Optional.of(mockUser));
        when(kycProfileRepository.existsByUserId(1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kycService.submitKyc(request, file, "customer02"));
        assertEquals("KYC profile already submitted", ex.getMessage());
        verify(cloudinaryService, never()).uploadImage(any(), any());
    }

    @Test
    void submitKyc_InvalidFileType_ThrowsException() {
        // Arrange
        KycRequest request = new KycRequest();
        request.setIdNumber("079999999999");
        request.setFullName("Nguyen Van C");

        MultipartFile file = new MockMultipartFile("idCardFile", "doc.pdf", "application/pdf", "data".getBytes());

        when(userRepository.findByUsername("customer02")).thenReturn(Optional.of(mockUser));
        when(kycProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(kycProfileRepository.existsByIdNumber("079999999999")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kycService.submitKyc(request, file, "customer02"));
        assertEquals("Only JPEG/PNG images are allowed", ex.getMessage());
    }

    @Test
    void reviewKyc_AlreadyReviewed_ThrowsException() {
        // Arrange
        mockProfile.setStatus(Status.CONFIRM); // đã duyệt rồi
        when(kycProfileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> kycService.reviewKyc(1L, Status.CONFIRM));
        assertEquals("KYC profile has already been reviewed", ex.getMessage());
    }

    @Test
    void reviewKyc_Confirm_UpdatesUserIsKycTrue() {
        // Arrange
        when(kycProfileRepository.findById(1L)).thenReturn(Optional.of(mockProfile));
        when(kycProfileRepository.save(any(KycProfile.class))).thenReturn(mockProfile);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        KycResponseDto result = kycService.reviewKyc(1L, Status.CONFIRM);

        // Assert
        assertEquals(Status.CONFIRM, result.getStatus());
        assertTrue(mockUser.getIsKyc());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void getMyKyc_ProfileNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("customer02")).thenReturn(Optional.of(mockUser));
        when(kycProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> kycService.getMyKyc("customer02"));
    }
}