/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.repository;

import com.bixterprise.gateway.domain.WorkSpace;
import com.bixterprise.gateway.utils.PhoneOperator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
/**
 *
 * @author champlain
 */
@Repository
public interface WorkSpaceRepository extends JpaRepository<WorkSpace, Long> {

    public WorkSpace findOneByHop(int hop);

    @Query("select ws from WorkSpace ws join TransactionActivity ta on ws.activity = ta join AutomateAgents ag on ta.agentPhone = ag and ag.phoneOperator = :operateur")
    public Page<WorkSpace> findOneByAgentPhoneOperator(@Param("operateur") PhoneOperator operateur, Pageable pageable);

    
}
