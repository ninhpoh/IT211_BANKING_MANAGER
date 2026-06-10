package com.banking_management.repository;

import com.banking_management.model.entity.KycProfile;
import com.banking_management.model.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {

    Optional<KycProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByIdNumber(String idNumber);

    Page<KycProfile> findByStatus(Status status, Pageable pageable);
}