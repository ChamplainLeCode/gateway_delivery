package com.bixterprise.gateway.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bixterprise.gateway.domain.AgentMessage;

@Repository
public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long>{

	

	@Query("from AgentMessage ta where ta.date between :debut and :fin")
	Page<AgentMessage> findAllByCreatedAt(Pageable page, @Param("debut") Date ddebut, @Param("fin") Date dfin);

	@Query("from AgentMessage ta where ta.date between :debut and :fin")
	List<AgentMessage> findAllByCreatedAt(@Param("debut") Date ddebut, @Param("fin") Date dfin);

	@Query("from AgentMessage ta where ta.date >= :debut")
	Page<AgentMessage> findAllAfterDate(Pageable page, @Param("debut") Date ddebut);

	@Query("from AgentMessage ta where ta.date <= :fin")
	Page<AgentMessage> findAllBeforeDate(Pageable page, @Param("fin") Date time);
}
