package com.bixterprise.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.Transactions;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, String>{

}
