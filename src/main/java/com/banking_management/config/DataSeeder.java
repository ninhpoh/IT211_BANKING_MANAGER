package com.banking_management.config;

import com.banking_management.model.entity.Account;
import com.banking_management.model.entity.Role;
import com.banking_management.model.entity.User;
import com.banking_management.repository.AccountRepository;
import com.banking_management.repository.RoleRepository;
import com.banking_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedUsers();
        seedAccounts();
    }

    private void seedRoles() {
        createRoleIfNotExists("ADMIN", "Admin role");
        createRoleIfNotExists("STAFF", "Staff role");
        createRoleIfNotExists("CUSTOMER", "Customer role");
    }

    private void seedUsers() {
        createUserIfNotExists("admin",      "12345678", "admin@bank.com",      "0900000001", "ADMIN");
        createUserIfNotExists("staff01",    "12345678", "staff01@bank.com",    "0900000002", "STAFF");
        createUserIfNotExists("customer01", "12345678", "customer01@bank.com", "0900000003", "CUSTOMER");
    }

    private void seedAccounts() {
        createAccountIfNotExists("1000000001", "customer01", 50000000.00, "123456");
        createAccountIfNotExists("1000000002", "customer01", 10000000.00, "123456");
    }

    // ===================== Helpers =====================

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
            log.info("[SEEDER] Created role: {}", name);
        }
    }

    private void createUserIfNotExists(String username, String password,
                                       String email, String phone, String roleName) {
        if (userRepository.existsByUsername(username)) return;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .phoneNumber(phone)
                .isActive(true)
                .isKyc(false)
                .role(role)
                .build());

        log.info("[SEEDER] Created user: {} ({})", username, roleName);
    }

    private void createAccountIfNotExists(String accountNumber, String ownerUsername,
                                          double initialBalance, String pin) {
        if (accountRepository.existsByAccountNumber(accountNumber)) return;

        User user = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + ownerUsername));

        accountRepository.save(Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.valueOf(initialBalance))
                .currency("VND")
                .transactionPin(passwordEncoder.encode(pin))
                .active(true)
                .user(user)
                .build());

        log.info("[SEEDER] Created account: {} for user: {}", accountNumber, ownerUsername);
    }
}