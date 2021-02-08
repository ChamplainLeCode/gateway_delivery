/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "agent_messages")
public class AgentMessage {
	
	@Id
	@GeneratedValue
	Long id;
	
	@ManyToOne
	AutomateAgents agent;
	
	String message;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date date; 
	

	public AgentMessage() {}

	public AgentMessage(Long id) {
		super();
		this.id = id;
	}

	public AgentMessage(Long id, AutomateAgents agent, String message, Date date) {
		super();
		this.id = id;
		this.agent = agent;
		this.message = message;
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AutomateAgents getAgent() {
		return agent;
	}

	public void setAgent(AutomateAgents agent) {
		this.agent = agent;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	
	

}
