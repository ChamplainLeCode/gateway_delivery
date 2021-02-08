/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;


import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 *
 * @author haranov Champlain
 */
@Entity
@Table(name = "agent_transaction")
@JsonAutoDetect(creatorVisibility = Visibility.PUBLIC_ONLY)
@NamedQueries({
    @NamedQuery(name = "AgentTransaction.findAll", query = "SELECT a FROM AgentTransaction a")})
public class AgentTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "amount")
    private float amount;
    @Basic(optional = false)
    @Lob
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "agent_phone", referencedColumnName = "phone")
    @ManyToOne(optional = false)
    private AutomateAgents agentPhone;

    public AgentTransaction() {
    }

    public AgentTransaction(Long id) {
        this.id = id;
    }

    public AgentTransaction(Long id, long amount, String description, Date createdAt) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    protected AutomateAgents getAgentPhone() {
        return agentPhone;
    }
    
    public AutomateAgents getAgent() {
    	return getAgentPhone();
    }

    public void setAgentPhone(AutomateAgents agentPhone) {
        this.agentPhone = agentPhone;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AgentTransaction)) {
            return false;
        }
        AgentTransaction other = (AgentTransaction) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gateway.models.AgentTransaction[ id=" + id + " ]";
    }
    
}
