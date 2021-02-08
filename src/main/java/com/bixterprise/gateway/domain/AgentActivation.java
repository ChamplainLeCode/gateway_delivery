/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;

/**
 *
 * @author champlain
 */

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.bixterprise.gateway.domain.enums.AgentStatus;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 *
 * @author Haranov Champlain
 */
@Entity
@Table(name = "agents_activation")
@JsonAutoDetect(creatorVisibility = Visibility.NON_PRIVATE)
public class AgentActivation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	Long id;
	
	@Column(columnDefinition = " Datetime default CURRENT_TIMESTAMP ") 
	@Basic(optional = false)
	@Temporal(TemporalType.TIMESTAMP)
	Calendar date = Calendar.getInstance();
	
	@JoinColumn(referencedColumnName = "phone", name = "agents", nullable = false)
	@ManyToOne(targetEntity = AutomateAgents.class)
	private AutomateAgents agent;
	
	@Enumerated(EnumType.STRING)
	@Basic(optional = false)
	AgentStatus status;
	
	@Column(nullable = true)
	String reason;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Calendar getDate() {
		return date;
	}
	public void setDate(Calendar date) {
		this.date = date;
	}
	public String getAgent() {
		return agent.getPhone();
	}
	public void setAgent(AutomateAgents agent) {
		this.agent = agent;
	}
	public AgentStatus getStatus() {
		return status;
	}
	public void setStatus(AgentStatus status) {
		this.status = status;
	}
	
	

}
