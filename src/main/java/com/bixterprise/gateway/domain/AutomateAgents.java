/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.bixterprise.gateway.domain.enums.AgentStatus;
import com.bixterprise.gateway.utils.PhoneOperator;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author haranov Champlain
 */
@Entity
@Table(name = "automate_agents")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AutomateAgents.findAll", query = "SELECT a FROM AutomateAgents a")})
public class AutomateAgents implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "phone")
    private String phone;
    @Basic(optional = false)
    @Column(name = "imei")
    private String imei;
    @Basic(optional = false)
    @Column(name = "token")
    private String fcm_token;
    @Column(name = "solde", columnDefinition = " BigInt not null default 0")
    private Float solde;
    @Basic(optional = false)
    @Column(name = "is_online")
    private boolean isOnline;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "agentPhone", fetch = FetchType.EAGER)
    private Set<TransactionActivity> transactionActivitySet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "agent", fetch = FetchType.EAGER)
    private Set<AgentActivity> agentActivitySet;
    @Transient
    String log;
    @Enumerated(EnumType.STRING)
    @Column(name="phoneOperator")
    private PhoneOperator phoneOperator;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = " Varchar(20) default \"ACTIVATE\"") 
    private AgentStatus status;
    
    public AutomateAgents() {
    	transactionActivitySet = new HashSet<TransactionActivity>();
    }

    public AutomateAgents(String phone) {
        this.phone = phone;
    }

    public AutomateAgents(String phone, String imei, boolean isOnline, Date createdAt, Date updatedAt) {
        this.phone = phone;
        this.imei = imei;
        this.isOnline = isOnline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public boolean getIs_online() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Float getBalance() {
		return solde;
	}

	public void setSolde(Float solde) {
		this.solde = solde;
	}

	public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    @XmlTransient
    protected Set<TransactionActivity> getTransactionActivitySet() {
        return transactionActivitySet;
    }

    public void setTransactionActivitySet(Set<TransactionActivity> transactionActivitySet) {
        this.transactionActivitySet = transactionActivitySet;
    }

    @XmlTransient
    protected Set<AgentActivity> getAgentActivitySet() {
        return agentActivitySet;
    }

    public void setAgentActivitySet(Set<AgentActivity> agentActivitySet) {
        this.agentActivitySet = agentActivitySet;
    }
    
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

    public String getFcm_token() {
		return fcm_token;
	}

	public void setFcm_token(String fcm_token) {
		this.fcm_token = fcm_token;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (phone != null ? phone.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutomateAgents)) {
            return false;
        }
        AutomateAgents other = (AutomateAgents) object;
        if ((this.phone == null && other.phone != null) || (this.phone != null && !this.phone.equals(other.phone))) {
            return false;
        }
        return true;
    }

    public void setPhoneOperator(PhoneOperator operator) {
        this.phoneOperator = operator;
    }
    public PhoneOperator getPhoneOperator() {
        return this.phoneOperator;
    }
    
    public boolean isMTN() {
        return this.phoneOperator != null && this.phoneOperator == PhoneOperator.MTN;
    }
    
    public boolean isOrange() {
        return this.phoneOperator != null && this.phoneOperator == PhoneOperator.ORANGE;
    }
    
    
	public HashMap toJSONString() {
                HashMap<String, Object> obj = new HashMap();
		obj.put("phone", phone);
		obj.put("imei", imei);
		obj.put("fcm_token", fcm_token);
		obj.put("balance", solde);
		obj.put("status", status);
		obj.put("is_online", isOnline);
		obj.put("createdAt", createdAt);
		obj.put("updatedAt", updatedAt);
		obj.put("log", log);
		obj.put("phoneOperator", phoneOperator);
		return obj;
	}

	public void setActive() {
		this.status = AgentStatus.ACTIVATE;
	}
	public void setDeactivate() {
		this.status = AgentStatus.DEACTIVATE;
	}
	public void setBlocked() {
		this.status = AgentStatus.BLOCKED;
	}
	public String getStatus() {
		return status.toString().toUpperCase();
	}
	public void setStatus(AgentStatus s) {
		status = s;
	}
	public AgentStatus status() {
		return status;
	}
    
    
}