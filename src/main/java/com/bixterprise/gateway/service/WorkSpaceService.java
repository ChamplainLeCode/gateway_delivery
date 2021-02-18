/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.service;

import com.bixterprise.gateway.domain.TransactionActivity;
import com.bixterprise.gateway.domain.WorkSpace;
import com.bixterprise.gateway.repository.WorkSpaceRepository;
import com.bixterprise.gateway.repository.TransactionActivityRepository;

import com.bixterprise.gateway.utils.PhoneOperator;
import com.bixterprise.gateway.utils.TransactionStatus;
import com.bixterprise.gateway.websocket.messages.MessageType;
import io.socket.client.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 *
 * @author champlain
 */
@Service
public class WorkSpaceService {
    
    public static String LOG_SEPARATOR = "$<<<>>>$";

    /**
     * Repository for workSpace db
     */
    @Autowired WorkSpaceRepository db;
    @Autowired TransactionActivityRepository transactionActivityRepository;
    
    /**
     * Indicate whether thread that logically iterate on workspace is actived
     * There is one way to launch thread and one to stop.
     * When a new WorkSpace is added, we set this to true only if it's not true
     * yet. We set to false when there is not Workspace after calling findNext
     */
    public static boolean launchMtnWorkSpaceThread = false;
    public static boolean launchOrangeWorkSpaceThread = false;
    
    /**
     * Lock on launchWorkSpaceThread, to insure that sigle thread will works
     */
    @Autowired @Qualifier("gatewayMtnWorkSpaceThreadLock") Lock mtnThreadLock;
    @Autowired @Qualifier("gatewayOrangeWorkSpaceThreadLock") Lock orangeThreadLock;
    @Autowired @Qualifier("gatewayDeliverer") Socket gatewayDeliverer;
    
    public static HashMap<String, Object> map = new HashMap<>();

    public WorkSpace save(TransactionActivity transac){
        
        WorkSpace workSpace = new WorkSpace();
            workSpace.setActivity(transac);
            workSpace.setAgent(transac.getAgentPhone());
            workSpace.setHop(0);
            workSpace.setStatus(TransactionStatus.PENDING);
        WorkSpace workSpaceSaved = db.save(workSpace);
        if(workSpaceSaved.getAgent().isMTN()){
            mtnThreadLock.lock();
            try{
                if( ! launchMtnWorkSpaceThread ){
                    gatewayDeliverer.emit("start/process/mtn");
                }
                return workSpaceSaved;
            } finally {
                mtnThreadLock.unlock();
            }
        }
        if(workSpaceSaved.getAgent().isOrange()){
            orangeThreadLock.lock();
            try{
                if( ! launchOrangeWorkSpaceThread ){
                    gatewayDeliverer.emit("start/process/orange");
                }
                return workSpaceSaved;
            } finally {
                orangeThreadLock.unlock();
            }
        }
        return workSpaceSaved;
    }
    
    /**
     * Find next WorkSpace for specific operator.
     * @param operateur
     * @return 
     */
    public WorkSpace findNextForOperator(PhoneOperator operateur){
        switch(operateur){
            case MTN:
            case ORANGE: 
                /**
                 * Here we first try to get transaction with low hop cost. 
                 * -> if there
                 * is not any transaction, it means that there is now jobs. then we
                 * could stop process.
                 * -> If workspace found is assign to TransactionActivity with
                 */
                Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.asc(com.bixterprise.gateway.domain.WorkSpace_.HOP)));
                Page<WorkSpace> workSpaces = db.findOneByAgentPhoneOperator(operateur, pageable);
                
                if(workSpaces == null || workSpaces.isEmpty()){
                    Lock locker = operateur == PhoneOperator.ORANGE ? orangeThreadLock : mtnThreadLock;
                    locker.lock();
                    try{
                        gatewayDeliverer.emit( ( operateur == PhoneOperator.ORANGE ? MessageType.ORANGE_THREAD_STOP : MessageType.MTN_THREAD_STOP).value);
                        return null;
                    }finally{
                        locker.unlock();
                    }
                }
                return workSpaces.getContent().get(0);

                
            default: return null;
        }
        
    }
    
    /**
     * Update workspace from Gateway Request Deliverer
     * @param ws WorkSpace to update
     * @return Updated WorkSpace
     */
    public WorkSpace updateWorkSpace(WorkSpace ws){
        
        return null;
    }
    
    /**
     * this method is used to display the state of workspace.
     * It's show status, and hop of each activity in workspace
     * @param pageable pageRequest
     * @return Page of WorkSpace
     */
    public Page<WorkSpace> currentState(Pageable pageable){
        
        
        return null;
    }

    public void updateWorkSpaceReceived(Long id) {
        WorkSpace workSpace = db  .findById(id).orElse(null);
            if(workSpace != null){
                workSpace.setHop(workSpace.getHop()+1);
                if(workSpace.getHop() == 4){
                    TransactionActivity ta = workSpace.getActivity();
                    ta.setStatus(TransactionStatus.FAILURE.toString());
                    ta.setLog(ta.getLog()+LOG_SEPARATOR+"Multiple Tentatives");
                    transactionActivityRepository.save(ta);
                    db.delete(workSpace);
                    return;
                }
                db.save(workSpace);
          }
    }
}
