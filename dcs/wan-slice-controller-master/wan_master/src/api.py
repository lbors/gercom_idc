import logging
import random
import socket
from timeit import default_timer as timer

from flask import Flask
from flask import request, jsonify
from flask import Response
from yaml_handler import yaml_to_dict, dict_to_yaml

from dao_handler import *
from agents_api import *

app = Flask(__name__)


if __name__ != '__main__':
    gunicorn_logger = logging.getLogger("gunicorn.error")
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)

    while not test_db_connection():
        app.logger.info("Testing db connection...")
        time.sleep(1)
    
    create_db()


@app.route("/")
def hello():

    agents_result = []

    for agent_id in AGENTS:
        status  = check_agent(agent_id)

        if status:
            status = "OK"
        else:
            status = "ERROR"
        
        agents_result.append({AGENTS[agent_id]["description"] : status})


    if test_db_connection():
        database_connection_status = "OK"
    else:
        database_connection_status = "ERROR"
    
    return jsonify({"rest_api" : "ok", "database_connection_status" : database_connection_status, 
                    "agents" : agents_result})


@app.route("/request_slice_part", methods=['POST'])
def request_slice_part():
    start = timer()

    app.logger.debug("request_slice_part")

    slice_req_data = yaml_to_dict(request.data)

    app.logger.info(slice_req_data)

    links = slice_req_data["slices"]["sliced"]["slice-part"]["net-slice-part"]["links"]
    dc_slice_part_id_a = links["dc-part1"]["slice-part-uuid"]
    dc_slice_part_id_b = links["dc-part2"]["slice-part-uuid"]

    dc_slice_controller_id_a = links["dc-part1"]["dc-slice-controller-id"]
    dc_slice_controller_id_b = links["dc-part2"]["dc-slice-controller-id"]

    wan_slice_part_id = create_wan_slice(dc_slice_part_id_a, dc_slice_part_id_b,
                                         dc_slice_controller_id_a, dc_slice_controller_id_b)

    response_result = {
        "status": "OK",
        "response": {
            "slice-part-id": { 
                "slice-controller-id": CONTROLLER_ID,
                "uuid": wan_slice_part_id
            }
        }

    }
    
    end = timer()
    app.logger.info("Elapsed time: " + str(end - start))

    return Response(dict_to_yaml(response_result), mimetype='text/x-yaml')


@app.route("/activate_slice_part", methods=['POST'])
def activate_slice_part():
    start = timer()

    slice_activate_data = yaml_to_dict(request.data)
    wan_slice_part_id = slice_activate_data["slices"]["sliced"]["slice-part-id"]["uuid"]

    wan_slice = get_wan_slice(int(wan_slice_part_id))

    app.logger.info("activating slice part with id "+wan_slice_part_id)
    #app.logger.info(wan_slice_to_dict(wan_slice))

    dc_slice_controller_id_a = int(wan_slice.dc_slice_controller_id_a)
    dc_slice_controller_id_b = int(wan_slice.dc_slice_controller_id_b)

    if check_agent(dc_slice_controller_id_a) and check_agent(dc_slice_controller_id_b):
    
        agentA_bridge_name = "br_" + wan_slice.dc_slice_part_id_a
        remote_address = AGENTS[dc_slice_controller_id_b]["agent_address"]

        resultA = create_tunnel(dc_slice_controller_id_a, remote_address, wan_slice.tunnel_key, agentA_bridge_name)
        app.logger.info("agentA : " + str(resultA))

        agentB_bridge_name = "br_" + wan_slice.dc_slice_part_id_b
        remote_address = AGENTS[dc_slice_controller_id_a]["agent_address"]
        resultB = create_tunnel(dc_slice_controller_id_b, remote_address, wan_slice.tunnel_key, agentB_bridge_name)
        app.logger.info("agentB : " + str(resultB))

        if resultA and resultB:
            response_result = {
                "status": "OK",
                "response": {
                    "slice-part-id": { 
                        "slice-controller-id": CONTROLLER_ID,
                        "uuid": wan_slice_part_id
                    }
                }
            }
            set_slice_activated(int(wan_slice_part_id))
            app.logger.info("slice activated")
        else:
            response_result = {
                "status": "ERROR",
                "message": "unknown error",
                "response": {
                    "slice-part-id": { 
                        "slice-controller-id": CONTROLLER_ID,
                        "uuid": wan_slice_part_id
                    }
                }
            }
            app.logger.error("slice not activated")
    
    else:
        response_result = {
                "status": "ERROR",
                "message": "agents connection error",
                "response": {
                    "slice-part-id": { 
                        "slice-controller-id": CONTROLLER_ID,
                        "uuid": wan_slice_part_id
                    }
                }
            }
        app.logger.error("slice not activated")
    
    end = timer()
    app.logger.info("Elapsed time: " + str(end - start))

    return Response(dict_to_yaml(response_result), mimetype='text/x-yaml')


@app.route("/slice_part", methods=['DELETE'])
def deactivate_slice_part():
    start = timer()

    slice_activate_data = yaml_to_dict(request.data)
    wan_slice_part_id = slice_activate_data["slices"]["sliced"]["slice-part-id"]["uuid"]

    wan_slice = get_wan_slice(int(wan_slice_part_id))

    app.logger.info("Deactivating slice part with id "+wan_slice_part_id)
    app.logger.info(wan_slice_to_dict(wan_slice))

    dc_slice_controller_id_a = int(wan_slice.dc_slice_controller_id_a)
    dc_slice_controller_id_b = int(wan_slice.dc_slice_controller_id_b)

    agentA_bridge_name = "br_" + wan_slice.dc_slice_part_id_a
    resultA = remove_tunnel(dc_slice_controller_id_a, wan_slice.tunnel_key, agentA_bridge_name)
    app.logger.info("agentA : " + str(resultA))

    agentB_bridge_name = "br_" + wan_slice.dc_slice_part_id_b
    resultB = remove_tunnel(dc_slice_controller_id_b, wan_slice.tunnel_key, agentB_bridge_name)
    app.logger.info("agentB : " + str(resultB))

    if resultA and resultB:
        response_result = {
                "status": "OK",
                "response": {
                    "slice-part-id": { 
                        "slice-controller-id": CONTROLLER_ID,
                        "uuid": wan_slice_part_id
                    }
                }
            }
        set_slice_deactivated(int(wan_slice_part_id))
        app.logger.info("slice deactivated")
    else:
        response_result = {
                "status": "ERROR",
                "response": {
                    "slice-part-id": { 
                        "slice-controller-id": CONTROLLER_ID,
                        "uuid": wan_slice_part_id
                    }
                }
            }

    end = timer()
    app.logger.info("Elapsed time: " + str(end - start))

    return Response(dict_to_yaml(response_result), mimetype='text/x-yaml')


@app.route("/remove_tunnel")
def sro_method():
    return jsonify({"result" : True})


if __name__ == "__main__":
    app.config["DEBUG"] = True
    app.run(host='0.0.0.0', port=8080)