package com.wirebarley.remittance.infra.jpa.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wirebarley.remittance.domain.transaction.TransactionType;
import com.wirebarley.remittance.infra.jpa.entity.TransactionEntity;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {

  @EntityGraph(attributePaths = { "account", "counterpartyAccount" })
  @Query("""
          select t
          from TransactionEntity t
          where t.account.accountNumber = :accountNumber
          order by t.createdAt desc
      """)
  Page<TransactionEntity> findLatest(
      @Param("accountNumber") String accountNumber,
      Pageable pageable);

  @Query("""
      select coalesce(sum(t.amount), 0)
      from TransactionEntity t
      where t.account.accountNumber = :accountNumber
        and t.type = :type
        and t.createdAt >= :from
        and t.createdAt < :to
      """)
  long sumAmountByTypeBetween(
      @Param("accountNumber") String accountNumber,
      @Param("type") TransactionType type,
      @Param("from") Instant from,
      @Param("to") Instant to);
}
