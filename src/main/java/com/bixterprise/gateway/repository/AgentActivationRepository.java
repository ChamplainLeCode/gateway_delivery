package com.bixterprise.gateway.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bixterprise.gateway.domain.AgentActivation;
import com.bixterprise.gateway.domain.AutomateAgents;

public interface AgentActivationRepository extends JpaRepository<AgentActivation, Long> {

	Page<AgentActivation> findAllByAgent(Pageable pageable, AutomateAgents phone); 

}
