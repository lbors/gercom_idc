import hug
import yaml
import grequests
import requests
import json
from controller.probe_strategy import *
import objectpath
import subprocess
from itertools import chain
from collections import defaultdict

from model.db import Mongo
import config_broker

# MongoDB collections used by Broker
database = Mongo(config_broker.MONGO_IP_PORT, config_broker.DB_NAME)


class SliceBroker: 

    def __init__(self):
        self.agents = {}
        #select the probe selection strategy to be use
        self.probe_strategy = ProbeStrategy(ConcreteStrategyB())
        agents_list = database.read(config_broker.COLLECTION_ACTIVE_AGENTS)
        self.status(agents_list)
 
    def status(self, active_agents):
        print("Check of agents actives")
        # reloading register in the broker
        request = ['ProviderId --> ']
        for agent in active_agents:
            print ("AGGENTT", agent)
            if type(agent) == dict:
                temp = agent['providerId']
            else:
                temp = agent
            temp = temp.split(':')
            ip = temp[0]
            port = temp[1]
            agent_run = subprocess.getoutput("nc -vz" + " " + ip + " " + port)
            '''
            for SO Debian
             if "succeeded" in agent_run:
                if type(agent) == dict:
                    self.agents[agent['providerId']] = agent
                else:
                    self.agents[agent] = agent
                request.append(ip + ":" + port)
            else:
                if "refused" in agent_run:
                    print("Port Offline: Delete")
                    database.remove({'providerId': {'$eq': ip + ":" + port}}, config_broker.COLLECTION_ACTIVE_AGENTS)
            '''
            #for SO Alpine
            if "open" in agent_run:
                if type(agent) == dict:
                    self.agents[agent['providerId']] = agent
                else:
                    self.agents[agent] = agent
                request.append(ip + ":" + port)
            else:
                print("Port Offline: Delete")
                database.remove({'providerId': {'$eq': ip + ":" + port}}, config_broker.COLLECTION_ACTIVE_AGENTS)
        return request

    def probe_agents(self, agents_pdts):

        print("Sending request to agents")
        r = []        
        print(agents_pdts)
        for agents, pdts in agents_pdts.items():
            r.append("http://"+ agents + "/pull_resource_offer?resourceDescriptor=" + json.dumps(pdts))
        print(r)
        rr = (grequests.get(u) for u in r)
        a = grequests.map(rr)
        return a


sb = SliceBroker()


# APIs exposed
@hug.post()
def locate_slice_resources(body):
    """called by the Slice Builder"""
    pdt_yaml = yaml.load(body['pdt'])
    pdt_json =  json.loads(json.dumps(pdt_yaml))

    jsonnn_tree = objectpath.Tree(pdt_json)

    database.insert(pdt_json, config_broker.COLLECTION_BUILDERS)

    #split the pdt msg it in the dc-slice-part and net-slice-part
    cost = tuple(jsonnn_tree.execute("$..'cost'"))
    time = tuple(jsonnn_tree.execute("$..'slice-timeframe'"))
    dc_slice_parts = tuple(jsonnn_tree.execute("$..'dc-slice-part'"))
    net_slice_parts = tuple(jsonnn_tree.execute("$..'net-slice-part'"))

    if dc_slice_parts:
        #select the agents to be query
        dc_agents_to_be_query = sb.probe_strategy.select_candidates(sb.agents, dc_slice_parts,'dc-slice-part', cost[0]['dc-model'], time)
        #dc_agents_to_be_query.append(cost, time)
        print("Resultado da selecao DC: ",dc_agents_to_be_query)
        #send the async request for the agents
        return_dc_push_resource_offer =  sb.probe_agents(dc_agents_to_be_query)
    
    if net_slice_parts:
        #select the agents to be query
        net_agents_to_be_query = sb.probe_strategy.select_candidates(sb.agents, net_slice_parts,'net-slice-part', cost[0]['net-model'], time)
        #net_agents_to_be_query.append(cost, time)
        print("Resultado da selecao NET: ",net_agents_to_be_query)

        #send the async request for the agents
        return_net_push_resource_offer = sb.probe_agents(net_agents_to_be_query)

    print("###########")

    return_msg = {'slice-parts-options':[]}
    
    for i in return_dc_push_resource_offer:
        i_json = json.loads(i.text)
        print("--> ", i_json)
        for dsp in i_json['dc-slice-part']:
            new_dc_slice_part = True
            for r in return_msg['slice-parts-options']:
                if 'dc-slice-choices' in r and r['dc-slice-choices']["name"] == dsp["name"]:
                    del dsp["name"]
                    r['dc-slice-choices']["alternatives"].append({"dc-slice-part":dsp}) 
                    new_dc_slice_part = False
                    break

            print(i_json)
            if new_dc_slice_part:
                name = dsp["name"]
                del dsp["name"]
                alternatives = []
                alternatives.append({"dc-slice-part": dsp})
                return_msg['slice-parts-options'].append({'dc-slice-choices':{"name": name, "alternatives":alternatives}})

    print("---------")
    for i in return_net_push_resource_offer:
        i_json = json.loads(i.text)
        print("--> ", i_json)
        for nsp in i_json['net-slice-part']:
            new_net_slice_part = True
            for r in return_msg['slice-parts-options']:
                if 'net-slice-choices' in r and r['net-slice-choices']["name"] == nsp["name"]:
                    del nsp["name"]
                    r['net-slice-choices']["alternatives"].append({"net-slice-part": nsp})
                    new_net_slice_part = False
                    break

            if new_net_slice_part:
                name = nsp["name"]
                del nsp["name"]
                alternatives = []
                alternatives.append({"net-slice-part": nsp})
                return_msg['slice-parts-options'].append({'net-slice-choices':{"name": name, "alternatives":alternatives}})

    print("RETORNO :", return_msg)
    return yaml.dump(return_msg)


    # save PDT msg in database (mongodb) to future funtionalities (to-do), salvar as msgs ptd no banco de dados
    # mongodb para futuras funcionalidades (falta-fazer).



@hug.get()
def register_provider(providerId: hug.types.text, providerType: hug.types.text, location: hug.types.text):
    """Provide an interface for the registration of Slice Agents to maintain an updated list of potential
    resource providers """

    print("Registering a new Agent...")
    new_agent = {"providerId": providerId, "location": location, "providerType": providerType}

    # Add the agent in the Broker's runtime list with providerId as the key.
    sb.agents[providerId] = new_agent

    # Save the agent in the DB
    database.insert(new_agent, config_broker.COLLECTION_AGENTS)

    # Include active agents in the new collection
    database.insert(new_agent, config_broker.COLLECTION_ACTIVE_AGENTS)
  
    print(sb.agents)
    return {'message': "Agent registered"}

@hug.get()
def status():
    """called by the Slice Builder"""
    active_agents = sb.agents
    res = json.dumps(sb.status(active_agents))
    print (res)
    return res
