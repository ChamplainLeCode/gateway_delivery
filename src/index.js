#!/usr/bin/env node


const app = require('express')()
const http = require('http').Server(app)
const io = require('socket.io')(http)

const { AgentPipe, Agent, SocketAgent }  = require('./models/agent') 

const { PhoneOperator } = require('./models/phone_operator')

const { Pipe, OperatorConnector } = require('./models/operator')

const log = require('console').log
const { emit } = require('process')

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html')
})

var gatewayServer

let onlineUnknownAgents = {"k_is":{String, SocketAgent}}

let onlineAgents = {
    MTN: new OperatorConnector(),
    ORANGE: new OperatorConnector()
}


io.of('/server').on('connection', (s) => {

    delete onlineUnknownAgents.k_is
    // let futureTask
    // let thread = setInterval(() => {
    //     if(futureTask){
    //         clearTimeout(futureTask)
    //     }
    //     futureTask = setTimeout(() => {
    //         log(
    //             `\n\n#######################################################\n#################### ONLINE PIPE ONLINE START ${(new Date)}  ######################\n\n`)
    //             log(onlineAgents)
    //         log(
    //             `\n\n#######################################################\n#################### ONLINE PIPE ONLINE END ${(new Date)}  ######################\n\n`)
    //             log(onlineUnknownAgents)
    //         log(
    //                 '\n\n#######################################################\n#################### ONLINE PIPE OFFLINE END   ######################\n\n')
    //             }, 5000)
    // }, 60000)


    let mtnThreadId
    let orangeThreadId
    const threadDelay = 30000;

    function startMtnThread(){
        if(mtnThreadId){
            clearInterval(mtnThreadId)
        }
        mtnThreadId = setInterval(() => {
            log(
                `\n\n#######################################################\n#################### MTN THREAD RUN START ${(new Date)}  ######################\n\n`)
            gatewayServer.emit('command/for/mtn')
            log(
                `\n\n#######################################################\n#################### MTN THREAD RUN END    ${(new Date)} ######################\n\n`)

        }, threadDelay)
    }

    function startOrangeThread(){
        if(orangeThreadId){
            clearInterval(orangeThreadId)
        }
        orangeThreadId = setInterval(() => {
            log(
                `\n\n#######################################################\n#################### ORANGE THREAD RUN START ${(new Date)}  ######################\n\n`)
            gatewayServer.emit('command/for/orange')
            log(
                `\n\n#######################################################\n#################### ORANGE THREAD RUN END    ${(new Date)} ######################\n\n`)

        }, threadDelay)
        log(`\n\n########################################\n### THREAD ORANGE = ${orangeThreadId}\n#######################################`)
    }

    function stopMtnThread() {
        clearInterval(mtnThreadId)
    }

    function stopOrangeThread() {
        clearInterval(orangeThreadId)
    }

    gatewayServer = s

    log('a user connected')

    gatewayServer.on('disconnect', () => {
        log('user disconnected')
        stopMtnThread()
        stopOrangeThread()
        // clearInterval(thread)
    })

    gatewayServer.on('state', () => {
        console.log(onlineAgents.MTN.pipes)
        console.log(onlineAgents.ORANGE.pipes)
    })
    gatewayServer.on('logout', (agentMap) => {
        log('\n############  DISCONNECT ###############\n')
        log(agentMap)
        log('\n#######################################')
    })
    gatewayServer.on('login', (agentMap) => {
        log('\n############################\n### LOGIN -> ')
        log(agentMap)

        let pipe = onlineUnknownAgents[agentMap.pipe].socket;
        if(agentMap.status){
            let agent = new Agent(agentMap.data)
            if(agent.isMTN){
                onlineAgents.MTN.add(agent.fcm_token, new Pipe(onlineUnknownAgents[agent.fcm_token], agent))
                delete onlineUnknownAgents[agentMap.pipe]
            }else if(agent.isOrange){
                onlineAgents.ORANGE.add(agent.fcm_token, new Pipe(onlineUnknownAgents[agent.fcm_token], agent))
                delete onlineUnknownAgents[agentMap.pipe]
            }
            console.log(agent)
        }
        delete agentMap.pipe
        pipe.emit('login', agentMap)

    })

    gatewayServer.on('start/process/orange', () => {
        startOrangeThread()
        gatewayServer.emit('start/process/orange/started')
    })
    gatewayServer.on('start/process/mtn', () => {
        startMtnThread()
        gatewayServer.emit('start/process/mtn/started')
    })

    gatewayServer.on('stop/process/orange', () => {
        stopOrangeThread()
        gatewayServer.emit('stop/process/orange/stoped')
    })
    gatewayServer.on('stop/process/mtn', () => {
        stopMtnThread()
        gatewayServer.emit('stop/process/mtn/stoped')
    })

    gatewayServer.on('/command', (command) => {
        let operator = PhoneOperator.fromString(command.operator)
        let pipe = new OperatorConnector;
        switch ( operator ) {
            case PhoneOperator.ORANGE:
                gatewayServer.emit('command/received', command.workspace)
                pipe = onlineAgents.ORANGE
                break
            case PhoneOperator.MTN:
                gatewayServer.emit('command/received', command.workspace)
                pipe = onlineAgents.MTN
                break
            default:
                return;
        }
        delete command.operator
        delete command.workspace
        
        /**
         * On prend le premier agent dans la liste des agents
         */
        for (const socketId in pipe.pipes) {
            const pipeEntry = pipe.get(socketId);
            pipeEntry.agentSocket.socket.emit(pipeEntry.agentSocket.event, command)
            break;
        }
    })

    gatewayServer.on('command/notify', (onNotifyReceived) => {
        console.log(onNotifyReceived)
    })


    gatewayServer.on('update', (agentMap) => {
        log('\n############  UPDATE TRANSACTION ###############\n')
        log(agentMap)
        log('\n#######################################')
    })
})


io.of('/gateway').on('connection', (mobile) => {
    log(`############ CLIENT CONNECTED ##########`)
    /**
     * We ask to client to identify itself on every new connection
     */
    mobile.emit('who')

    /**
     * When agent try to identify itself
     */
    mobile.on('login', (data) => {
        gatewayServer.emit('login', data, mobile.id)
        /**
         * After emitting identification request to server, 
         * we add this pipe to unknown agent pipe's set
         */
        onlineUnknownAgents[mobile.id] = new SocketAgent(mobile, 'command')
    })

    /**
     * When agent ask for disconnection
     */
    mobile.on('logout', () => {
        log(`\n############# CLIENT LOGOUT ${mobile.id}  ############`)
        gatewayServer.emit('logout', mobile.id)
        
        /**
         * We remove current record inside the active records list
         */
        if( onlineAgents.MTN.contains(mobile.id))
            onlineAgents.MTN.remove(mobile.id)
        else (onlineAgents.ORANGE.contains(mobile.id))
            onlineAgents.ORANGE.remove(mobile.id)

    })

    /**
     * When Agent is disconnected
     */
    mobile.on('disconnect', () => {
        log(`\n############# CLIENT DISCONNECT ${mobile.id}  ############`)

        /**
         * We send agent disconnection to Gateway Server
         */
        gatewayServer.emit('logout', mobile.id)
        
        /**
         * We remove current record inside the active records list
         */
        if( onlineAgents.MTN.contains(mobile.id))
            onlineAgents.MTN.remove(mobile.id)
        else (onlineAgents.ORANGE.contains(mobile.id))
            onlineAgents.ORANGE.remove(mobile.id)
        
        log(`\n############# CLIENT DISCONNECT ${mobile.id} ############`)
    })


    /**
     * Le notify désigne l'accusé de reception de la transaction par le mobile
     * et donc il envoie l'identifiant de la transaction reçue
     * on transfert l'accusé de reception au serveur de la gateway
     */
    mobile.on('command/notify', (transactionId) => {
        log(`\n############  NOTIFY TRANSACTION ${transactionId} ###############\n`)
        gatewayServer.emit('command/notify', transactionId)
        log(`\n############  NOTIFY TRANSACTION SENT ${transactionId} ###############`)
    })

    /**
     * L'update désigne le status final de traitement de la transaction par le mobile
     * et donc il envoie l'identifiant de la transaction reçue, le status de la transaction 
     * et le message (log) renvoyé par l'opérateur
     * on transfert ces données au serveur de la gateway
     */
    mobile.on('command/update', (transactionResponse) => {
        log(`\n############  UPDATE TRANSACTION ${JSON.stringify(transactionResponse)} ###############\n`)
        gatewayServer.emit('command/update', transactionResponse)
        log(`\n############  UPDATE TRANSACTION SENT ${JSON.stringify(transactionResponse)} ###############`)
    })
})


http.listen(3000, () => {
log('listening on *:3000')
})
