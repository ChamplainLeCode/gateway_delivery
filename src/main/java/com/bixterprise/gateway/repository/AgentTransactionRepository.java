package com.bixterprise.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.AgentTransaction;

@Repository

public interface AgentTransactionRepository extends JpaRepository<AgentTransaction, Long>{

	
}
