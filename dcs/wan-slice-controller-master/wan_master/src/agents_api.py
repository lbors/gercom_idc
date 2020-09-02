import requests

from settings import *


def get_agent_url(dc_slice_controller_id):
    agent = AGENTS[dc_slice_controller_id]

    agent_url = "http://" + agent["agent_address"] + ":" + str(agent["agent_port"]) + "/"

    return agent_url

def create_tunnel(dc_slice_controller_id, remote_address, tunnel_key, bridge_name):
    agent_url = get_agent_url(dc_slice_controller_id)
    url = agent_url + "create_tunnel"
    payload = {"tunnel_key" : tunnel_key, "bridge_name" : bridge_name, "remote_address" : remote_address}
    
    try:
        response = requests.get(url, params = payload,  timeout = AGENTS_TIMEOUT)
        result =  response.json()

        if result["result"] == "created":
            return True
        return False

    except Exception as e:
        return False
 

def check_agent(dc_slice_controller_id):
    agent_url = get_agent_url(dc_slice_controller_id)
    url = agent_url
    
    try:
        response = requests.get(url, timeout = AGENTS_TIMEOUT)
        result = response.json()
    except Exception as e:
        return None

    return result

def remove_tunnel(dc_slice_controller_id, tunnel_key, bridge_name):
    agent_url = get_agent_url(dc_slice_controller_id)
    url = agent_url + "remove_tunnel"
    payload = {"tunnel_key" : tunnel_key, "bridge_name" : bridge_name}
    
    try:
        response = requests.get(url, params = payload, timeout = AGENTS_TIMEOUT)
        result =  response.json()

        if result["result"] == "removed":
            return True
        return False

    except Exception as e:
        return False