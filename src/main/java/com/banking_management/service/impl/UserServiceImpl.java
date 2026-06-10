package com.banking_management.service.impl;

import com.banking_management.exception.ResourceNotFoundException;
import com.banking_management.model.dto.request.UpdateUserRequest;
import com.banking_management.model.dto.response.UserResponseDto;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.User;
import com.banking_management.repository.RoleRepository;
import com.banking_management.repository.UserRepository;
import com.banking_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * QUAN TRỌNG: Class này implements cả UserService lẫn UserDetailsService.
 * Spring Security sẽ tự detect bean UserDetailsService từ đây.
 * KHÔNG tạo thêm bean UserDetailsService nào khác để tránh conflict.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // ===================== UserDetailsService =====================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));
    }

    // ===================== UserService =====================

    @Override
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAllUserDtos(pageable);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return userRepository.findUserDtoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getEmail() != null)       user.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null)  user.setPhoneNumber(request.getPhoneNumber());
        if (request.getIsActive() != null)     user.setIsActive(request.getIsActive());
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", request.getRoleId()));
            user.setRole(role);
        }

        userRepository.save(user);
        return userRepository.findUserDtoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}