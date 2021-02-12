/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bixterprise.gateway.config.audit;

import com.bixterprise.gateway.utils.VoidCallback;

/**
 *
 * @author champlain
 */
public class GatewayDelivererTimer implements Runnable {

    private final VoidCallback callback;

    public GatewayDelivererTimer(VoidCallback callback){
        this.callback = callback;
    }
    @Override
    public void run() {
        callback.call();
    }
    
}
