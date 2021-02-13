const { Agent, SocketAgent } = require('./agent');
class OperatorConnector {
    pipes = {};

    add(key, pipe = Pipe()) {
        this.pipes[key] = pipe;
        return this;
    }

    get(key = '') {
        return this.pipes[key];
    }

    remove(key = '') {
        delete this.pipes[key];
        return this;
    }

    contains(key = ''){
        return key in this.pipes;
    }
}

class Pipe {

    constructor(agentSocket = new SocketAgent, agent = new Agent){
        this.agentSocket = agentSocket;
        this.agent = agent;
    }
    agent =  new Agent;
    agentSocket = new SocketAgent

    toString(){
        return JSON.stringify({
            agentSocket: this.SocketAgent,
            agent: this.agent
        })
    }

}

module.exports = { OperatorConnector, Pipe }