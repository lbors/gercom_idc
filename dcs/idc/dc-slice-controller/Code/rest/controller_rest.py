from flask import Flask, Blueprint, request
from model.controller import Controller
from dao.controller_dao import ControllerDAO
from ruamel.yaml import YAML
from slice_creator import logs
import json
import yaml

controller = Blueprint('controller', 'controller', url_prefix='/controller')
controller_dao = ControllerDAO()

@controller.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    controller = json.loads(json.dumps(yaml.load(request.data)))["controller"]
    id = None
    if(controller["id"] != None): id = controller["id"]
    # Get all Controllers if uuid are null
    if(id == None):
        obj = Controller()
        result = controller_dao.select_all_controllers()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Controller__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Controller:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get controllers")
            return "-1", 404
    # Get a User
    controller = controller_dao.select_controller(id)
    if(controller): return controller.to_yaml(), 200 
    else: 
        logs.logger.error(controller_dao.get_msg())
        return "-1", 404

@controller.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404

    if(controller_dao.select_all_controllers()!=0):
        logs.logger.error("Controller already exists")
        return "-1", 404
    controller = json.loads(json.dumps(yaml.load(request.data)))["controller"]
    controller_obj = Controller(controller["id"], controller["role"], controller["configuration_protocol"], controller['configuration_interface'], controller['provider'])
    if(controller_dao.insert_controller(controller_obj)): return controller_obj.to_yaml(), 201
    else: 
        logs.logger.error(controller_dao.get_msg())
        return "-1", 404

@controller.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    controller = json.loads(json.dumps(yaml.load(request.data)))["controller"]
    controller_obj = Controller(controller["id"], controller["role"], controller["configuration_protocol"], controller['configuration_interface'])
    controller_obj.set_id(controller["id"])
    if(controller_dao.select_controller(controller_obj.get_id())):

        if(controller_dao.update_controller(controller_obj)): return controller_obj.to_yaml(), 201
        else: 
            logs.logger.error(controller_dao.get_msg())
            return "-1", 404
    else:
        logs.logger.error(controller_dao.get_msg())
        return "-1", 404

@controller.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    controller = json.loads(json.dumps(yaml.load(request.data)))["controller"]
    id = None
    if(controller["id"] != None): id = controller["id"]
    if(controller_dao.select_controller(id)):
        if(controller_dao.delete_controller(id)): 
            logs.logger.info(controller_dao.get_msg())
            return id, 200
        else: 
            logs.logger.error(controller_dao.get_msg())
            return "-1", 404
    else:
        logs.logger.error(controller_dao.get_msg())
        return "-1", 404