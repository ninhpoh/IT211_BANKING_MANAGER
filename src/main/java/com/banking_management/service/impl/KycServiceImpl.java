package com.banking_management.service.impl;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.KycRequest;
import com.banking_management.model.dto.response.KycResponseDto;
import com.banking_management.model.entity.KycProfile;
import com.banking_management.model.entity.Status;
import com.banking_management.model.entity.User;
import com.banking_management.repository.KycProfileRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.CloudinaryService;
import com.banking_management.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * FR-04: Nộp hồ sơ eKYC + upload ảnh lên Cloudinary
     */
    @Override
    @Transactional
    public KycResponseDto submitKyc(KycRequest request, MultipartFile idCardFile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (kycProfileRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("KYC profile already submitted");
        }
        if (kycProfileRepository.existsByIdNumber(request.getIdNumber())) {
            throw new IllegalArgumentException("ID number already registered");
        }

        // Validate file
        validateFile(idCardFile);

        // Upload lên Cloudinary
        String fileUrl = cloudinaryService.uploadImage(idCardFile, "banking/kyc");

        KycProfile profile = KycProfile.builder()
                .idNumber(request.getIdNumber())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .sex(request.getSex())
                .address(request.getAddress())
                .idCardFrontUrl(fileUrl)
                .status(Status.PENDING)
                .user(user)
                .build();

        KycProfile saved = kycProfileRepository.save(profile);
        return mapToDto(saved);
    }

    /**
     * FR-09: Duyệt hồ sơ eKYC (STAFF/ADMIN)
     */
    @Override
    @Transactional
    public KycResponseDto reviewKyc(Long kycId, Status decision) {
        KycProfile profile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found: " + kycId));

        if (profile.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("KYC profile has already been reviewed");
        }
        if (decision != Status.CONFIRM && decision != Status.REJECT) {
            throw new IllegalArgumentException("Decision must be CONFIRM or REJECT");
        }

        profile.setStatus(decision);
        profile.setVerifiedAt(LocalDateTime.now());

        // Cập nhật isKyc của User
        User user = profile.getUser();
        user.setIsKyc(decision == Status.CONFIRM);
        userRepository.save(user);

        kycProfileRepository.save(profile);
        return mapToDto(profile);
    }

    @Override
    public Page<KycResponseDto> getPendingKyc(Pageable pageable) {
        return kycProfileRepository.findByStatus(Status.PENDING, pageable)
                .map(this::mapToDto);
    }

    @Override
    public KycResponseDto getMyKyc(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        KycProfile profile = kycProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC profile not found"));
        return mapToDto(profile);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ID card image is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPEG/PNG images are allowed");
        }
    }

    private KycResponseDto mapToDto(KycProfile p) {
        return KycResponseDto.builder()
                .id(p.getId())
                .idNumber(p.getIdNumber())
                .fullName(p.getFullName())
                .dob(p.getDob())
                .sex(p.getSex())
                .address(p.getAddress())
                .idCardFrontUrl(p.getIdCardFrontUrl())
                .status(p.getStatus())
                .verifiedAt(p.getVerifiedAt())
                .createdAt(p.getCreatedAt())
                .username(p.getUser().getUsername())
                .build();
    }
}