package com.banking_management.service;

import com.banking_management.model.dto.request.UpdateUserRequest;
import com.banking_management.model.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponseDto> getAllUsers(Pageable pageable);
    UserResponseDto getUserById(Long id);
    UserResponseDto updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}