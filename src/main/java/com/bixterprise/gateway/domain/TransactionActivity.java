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

import java.util.HashMap;



/**
 *
 * @author haranov Champlain
 */
@Entity
@Table(name = "transaction_activity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TransactionActivity.findAll", query = "SELECT t FROM TransactionActivity t")})
public class TransactionActivity implements Serializable {

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
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @Lob
    @Column(name = "log")
    private String log;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Basic(optional = false)
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "agent_phone", referencedColumnName = "phone", nullable = true)
    @ManyToOne(fetch = FetchType.EAGER)
    private AutomateAgents agentPhone;
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Transactions transactionId;
    
    public TransactionActivity() {
    }

    public TransactionActivity(Long id) {
        this.id = id;
    }

    public TransactionActivity(Long id, long amount, String status, String log, Date createdAt, Date updatedAt) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.log = log;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public AutomateAgents getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(AutomateAgents agentPhone) {
        this.agentPhone = agentPhone;
    }

    public Transactions getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Transactions transactionId) {
        this.transactionId = transactionId;
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
        if (!(object instanceof TransactionActivity)) {
            return false;
        }
        TransactionActivity other = (TransactionActivity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gateway.models.TransactionActivity[ id=" + id + " ]";
    }
    
    public Object toHashMap()  {
    	HashMap obj = new HashMap();
    	obj.put("id", id);
    	obj.put("amout", amount);
    	obj.put("createdAt", createdAt);
    	obj.put("updatedAt", updatedAt);
    	obj.put("log", log);
    	obj.put("status", status);
    	obj.put("transactionId", transactionId == null ? null : transactionId.toHashMap());
    	obj.put("agentPhone", agentPhone == null ? null : agentPhone.toJSONString());
    	return obj;
    }
    
}
