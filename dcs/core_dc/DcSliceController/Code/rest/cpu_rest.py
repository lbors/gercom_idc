from flask import Flask, Blueprint, request
from model.cpu import Cpu
from dao.cpu_dao import CpuDAO
from ruamel.yaml import YAML
from slice_creator import logs
import json
import yaml

cpu = Blueprint('cpu', 'cpu', url_prefix='/cpu')
cpu_dao = CpuDAO()

@cpu.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    cpu = json.loads(json.dumps(yaml.load(request.data)))["cpu"]
    id = None
    if(cpu["uuid"] != None): id = cpu["uuid"]
    # Get all Cpus if uuid are null
    if(id == None):
        obj = Cpu()
        result = cpu_dao.select_all_cpus()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Cpu__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "CPU:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get cpu")
            return "-1", 404
    # Get a User
    cpu = cpu_dao.select_cpu(id)
    if(cpu): return cpu.to_yaml(), 200 
    else: 
        logs.logger.error(cpu_dao.get_msg())
        return "-1", 404

@cpu.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    cpu = json.loads(json.dumps(yaml.load(request.data)))["cpu"]
    cpu_obj = Cpu(cpu["cycles"], cpu["cores"], cpu["model"], cpu['architecture'], cpu['instruction_set'])
    if(cpu_dao.insert_cpu(cpu_obj)): return cpu_obj.to_yaml(), 201
    else: 
        logs.logger.error(cpu_dao.get_msg())
        return "-1", 404


@cpu.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    cpu = json.loads(json.dumps(yaml.load(request.data)))["cpu"]
    cpu_obj = Cpu(cpu["cycles"], cpu["cores"], cpu["model"], cpu['architecture'], cpu['instruction_set'])
    cpu_obj.set_uuid(cpu["uuid"])
    if(cpu_dao.select_cpu(cpu_obj.get_uuid())):
        if(cpu_dao.update_cpu(cpu_obj)): return cpu_obj.to_yaml(), 201
        else: 
            logs.logger.error(cpu_dao.get_msg())
            return "-1", 404
    else:
        logs.logger.error(cpu_dao.get_msg())
        return "-1", 404


@cpu.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    cpu = json.loads(json.dumps(yaml.load(request.data)))["cpu"]
    id = None
    if(cpu["uuid"] != None): id = cpu["uuid"]
    if(cpu_dao.select_cpu(id)):
        if(cpu_dao.delete_cpu(id)): 
            logs.logger.info(cpu_dao.get_msg())
            return id, 200
        else: 
            logs.logger.error(cpu_dao.get_msg())
            return "-1", 404
    else:
        logs.logger.error(cpu_dao.get_msg())
        return "-1", 404
