const { Socket } = require("socket.io");

class AgentPipe {
    socket;
    event;

    constructor(socket, event) {
        this.socket = socket;
        this.event = event;
    }
}

class Agent {
    createdAt = null;
    balance = null;
    phone = null;
    log = null;
    fcm_token = null;
    phoneOperator = null;
    imei = null;
    is_online = null;
    status = null;
    updatedAt = null;
    
    constructor(map = null){
        if(map){
            this.createdAt = map.createdAt;
            this.balance = map.balance;
            this.phone = map.phone;
            this.log = map.log;
            this.fcm_token = map.fcm_token;
            this.phoneOperator = map.phoneOperator;
            this.imei = map.imei;
            this.is_online = map.is_online;
            this.status = map.status;
            this.updatedAt = map.updatedAt;
        }
    }

    get isMTN(){
        return this.phoneOperator?.startsWith('M') ?? false;
    }
    
    get isOrange(){
        return this.phoneOperator?.startsWith('O') ?? false;
    }
    
}

class SocketAgent {
    constructor(socket, event = ''){
        this.socket = socket;
        this.event = event;
    }

    toString(){
        return {
            socket: this.socket,
            event: this.event
        };
    }
}

module.exports = {AgentPipe, Agent, SocketAgent};