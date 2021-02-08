/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
@Table(name = "transactions")
//@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Transactions.findAll", query = "SELECT t FROM Transactions t")})
public class Transactions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id")
    private String id;
    @Basic(optional = false)
    @Column(name = "receiver_phone")
    private String receiverPhone;
    @Basic(optional = false)
    @Column(name = "amount")
    private Float amount;
    @Basic(optional = false)
    @Column(name = "url")
    String url;
    @Basic(optional = false)
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = Calendar.getInstance().getTime();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transactionId", fetch = FetchType.EAGER)
    private List<TransactionActivity> transactionActivityList;

    public Transactions() {
    	transactionActivityList = new LinkedList<TransactionActivity>();
    }

    public Transactions(String id) {
        this.id = id;
    }

    public Transactions(String id, String receiverPhone, float amount, Date createdAt) {
        this.id = id;
        this.receiverPhone = receiverPhone;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getReference() {
        return id;
    }

    public void setReference(String id) {
        this.id = id;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }


    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

   // @XmlTransient
    protected List<TransactionActivity> getTransactionActivityList() {
        return transactionActivityList;
    }

    public void setTransactionActivityList(List<TransactionActivity> transactionActivityList) {
        this.transactionActivityList = transactionActivityList;
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
        if (!(object instanceof Transactions)) {
            return false;
        }
        Transactions other = (Transactions) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gateway.models.Transactions[ id=" + id + " ]";
    }

    public Object toHashMap()  {
            HashMap obj = new HashMap();
            obj.put("id", id);
            obj.put("amount", amount);
            obj.put("createdAt", createdAt);
            obj.put("receiverPhone", receiverPhone);
            obj.put("url", url);
            return obj;

    }
    
}
