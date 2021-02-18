/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.config;

import com.bixterprise.gateway.service.WebSocketService;
import com.bixterprise.gateway.websocket.messages.MessageType;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 *
 * @author champlain
 */
@Component
public class WebSocketConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfigurer.class);

    
    
    @Bean(name="gatewayDeliverer")
    public Socket connectToServer(@Value("${microservice.deliverer.url}") String url) throws URISyntaxException{

        log.info("\n\n#########################################################\n###  Connecting to GatewayDeliverer START\n#########################################################");

        Socket socket = IO.socket(url); 
        socket.connect();
        
        log.info("\n\n#########################################################\n###  Connecting to GatewayDeliverer END\n#########################################################");
        return socket;
    }

    @Bean(name="gatewayDelivererConfiguration")
    public boolean status(@Qualifier("gatewayDeliverer") Socket socket, WebSocketService service){
        log.info("\n\n#########################################################\n###  Configuring Messages with GatewayDeliverer START\n#########################################################");
        Emitter on = socket
                .on(MessageType.SERVER_DISCONNECTED.value, service::serverDisconnected)
                .on(MessageType.LOGIN.value, service::onAgentLogin)
                .on(MessageType.LOGOUT.value, service::onAgentLogout)
                .on(MessageType.NEXT.value, service::onTransactionGetNextCommand)
                .on(MessageType.NEXT_RECEIVED.value, service::onTransactionGetNextCommandReceived)
                .on(MessageType.ORANGE_THREAD_STARTED.value, service::onOrangeThreadStarted)
                .on(MessageType.MTN_THREAD_STARTED.value, service::onMtnThreadStarted)
                .on(MessageType.ORANGE_THREAD_STOPED.value, service::onOrangeThreadStoped)
                .on(MessageType.MTN_THREAD_STOPED.value, service::onMtnThreadStoped)
                .on(MessageType.ASK_COMMAND_FOR_ORANGE.value, service::onAskCommandForOrange)
                .on(MessageType.ASK_COMMAND_FOR_MTN.value, service::onAskCommandForMtn)
                .on(MessageType.COMMAND_FOR_RECEIVED.value, service::onCommandForReceived)
                .on(MessageType.NOTIFY.value, service::onTransactionReceive)
                .on(MessageType.UPDATE.value, service::onTransactionUpdate);
                
        log.info("\n\n#########################################################\n###  Connecting Messages with GatewayDeliverer END\n#########################################################");
        return true;
    }
    
    @Bean(name="gatewayAgentListLock")
    public Lock lock(){
        Lock queueLock = new ReentrantLock(true);
        return queueLock;
        
    }
    
    @Bean(name="gatewayMtnWorkSpaceThreadLock")
    public Lock mtnWorkSpaceThreadLock(){
        Lock queueLock = new ReentrantLock(true);
        return queueLock;
        
    }
    
    @Bean(name="gatewayOrangeWorkSpaceThreadLock")
    public Lock orangeWorkSpaceThreadLock(){
        Lock queueLock = new ReentrantLock(true);
        return queueLock;
    }
    
    @Bean(name="gatewayOnlineAgentLock")
    public Lock gatewayOnlineAgentLock(){
        Lock queueLock = new ReentrantLock(true);
        return queueLock;   
    }
    
    
}
