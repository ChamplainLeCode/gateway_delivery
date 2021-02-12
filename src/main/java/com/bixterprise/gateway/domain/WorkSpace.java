/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.domain;

import com.bixterprise.gateway.utils.TransactionStatus;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * WorkSpace. désigne l'espace de travail du point de vu d'un ensemble de transaction en activité 
 * Pour mieux aborder ce concept! il faut l'aborder comme la commutation de processus! ou ici les processus 
 * sont les instances de WorkSpace. Dans l'idée! on admet pour erreur le fait qu'on puisse envoyer une 
 * transaction à un agent mobile (Téléphone) et celui ci n''accuse pas la reception (Opération dont le status)
 * est <b>Notify</b> ou <b>Update</b>. Notez qu'à l'envoi de la transaction on va dire quelle a une valeur pondérée de 1.
 * apres que l'horloge de commutation (Timer) pour envoyer la prochaine transaction soit arrivée on va selectionner
 * un transaction suivant l'algorithme  inverse de LRU (Least Recently Used). donc on va charger la dernière page à 
 * n'avoir été utilisée     
 * @author champlain
 */
@Entity
@Table(name = "workspace")
public class WorkSpace implements Serializable {
    
    @Id
    @GeneratedValue
    Long id;
    
    @JoinColumn
    @OneToOne
    TransactionActivity activity;
    
    @JoinColumn
    @OneToOne
    AutomateAgents agent;
    
    @Enumerated(EnumType.STRING)
    TransactionStatus status;
    
    @Column
    int hop;
    
    public WorkSpace(){
        status = TransactionStatus.PENDING;
        hop   = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionActivity getActivity() {
        return activity;
    }

    public void setActivity(TransactionActivity activity) {
        this.activity = activity;
    }

    public AutomateAgents getAgent() {
        return agent;
    }

    public void setAgent(AutomateAgents agent) {
        this.agent = agent;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public int getHop() {
        return hop;
    }

    public void setHop(int hop) {
        this.hop = hop;
    }
    
    
    
    
}
