package com.banking_management.repository;

import com.banking_management.model.dto.response.UserResponseDto;
import com.banking_management.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * JPQL Constructor Projection - chỉ SELECT các cột cần thiết,
     * không load toàn bộ Entity vào RAM.
     */
    @Query("""
            SELECT new com.banking_management.model.dto.response.UserResponseDto(
                u.id, u.username, u.email, u.phoneNumber,
                u.isActive, u.isKyc, u.role.name, u.createdAt
            )
            FROM User u
            """)
    Page<UserResponseDto> findAllUserDtos(Pageable pageable);

    @Query("""
            SELECT new com.banking_management.model.dto.response.UserResponseDto(
                u.id, u.username, u.email, u.phoneNumber,
                u.isActive, u.isKyc, u.role.name, u.createdAt
            )
            FROM User u
            WHERE u.id = :id
            """)
    Optional<UserResponseDto> findUserDtoById(Long id);
}