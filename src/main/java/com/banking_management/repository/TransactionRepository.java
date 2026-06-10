package com.banking_management.repository;

import com.banking_management.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * UC-06: Truy vấn tất cả giao dịch mà tài khoản tham gia
     * với tư cách bên gửi (fromAccount) HOẶC bên nhận (toAccount).
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findAllByAccountId(Long accountId, Pageable pageable);

    boolean existsByTransactionCode(String transactionCode);
}