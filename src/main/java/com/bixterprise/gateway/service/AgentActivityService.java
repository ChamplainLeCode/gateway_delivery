/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.service;

import com.bixterprise.gateway.domain.AgentActivity;
import com.bixterprise.gateway.repository.AgentActivityRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author champlain
 */
@Service
public class AgentActivityService {

    AgentActivityRepository activityRepository;
    
    public void save(AgentActivity activ) {
        activityRepository.save(activ);
    }
    
}
