package com.bixterprise.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.AgentActivity;

@Repository
public interface AgentActivityRepository extends JpaRepository<AgentActivity, Long>{

}
