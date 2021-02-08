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
import javax.persistence.FetchType;
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author haranov Champlain
 */
@Entity
@Table(name = "agent_activity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AgentActivity.findAll", query = "SELECT a FROM AgentActivity a")})
public class AgentActivity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Basic(optional = false)
    @Column(name = "context")
    private String context;
    @Basic(optional = false)
    @Lob
    @Column(name = "log")
    private String log;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "agent", referencedColumnName = "phone")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AutomateAgents agent;

    public AgentActivity() {
    }

    public AgentActivity(Long id) {
        this.id = id;
    }

    public AgentActivity(Long id, String context, String log, Date createdAt) {
        this.id = id;
        this.context = context;
        this.log = log;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AutomateAgents getAgent() {
        return agent;
    }

    public void setAgent(AutomateAgents agent) {
        this.agent = agent;
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
        if (!(object instanceof AgentActivity)) {
            return false;
        }
        AgentActivity other = (AgentActivity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gateway.models.AgentActivity[ id=" + id + " ]";
    }
    
}
