package com.bixterprise.gateway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.AutomateAgents;
import com.bixterprise.gateway.domain.enums.AgentStatus;

@Repository
public interface AgentRepository extends JpaRepository<AutomateAgents, String> {

	@Query("from AutomateAgents aa where aa.imei = :imei")
	Optional<AutomateAgents> findByImei(@Param("imei") String imei);

	@Query("from AutomateAgents aa where aa.solde >= :amount and aa.isOnline = true")
	List<AutomateAgents> findAgentForTransaction(@Param("amount") float amount);

	@Query("from AutomateAgents aa where aa.phone = :phone and aa.imei = :imei")
	AutomateAgents doLogin(@Param("phone") String phone, @Param("imei") String imei);
	
	@Query("Select status from AutomateAgents where phone = :phone")
	AgentStatus getStatusByPhone(@Param("phone") String phone);

	AutomateAgents findAutomateAgentsByPhone(String phone);
}
