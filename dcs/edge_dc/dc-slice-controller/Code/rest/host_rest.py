from flask import Flask, Blueprint, request
from model.host import Host
from model.cpu import Cpu
from dao.host_dao import HostDAO
from dao.cpu_dao import CpuDAO
from ruamel.yaml import YAML
from slice_creator import validate_yaml, logs
import json
import yaml

host = Blueprint('host', 'host', url_prefix='/host')
host_dao = HostDAO()
cpu_dao = CpuDAO()

@host.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    host = json.loads(json.dumps(yaml.load(request.data)))["host"]
    id = None
    if(host["uuid"] != None): id = host["uuid"]
    # Get all Templates if uuid are null
    if(id == None):
        obj = Host()
        result = host_dao.select_all_hosts()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Host__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Host:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get host")
            return "-1", 404
    # Get a User
    host = host_dao.select_host(id)
    if(host): return host.to_yaml(), 200
    else: 
        logs.logger.error(host_dao.get_msg())
        return "-1", 404

@host.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    host = json.loads(json.dumps(yaml.load(request.data)))["host"]
    if(validate_yaml.valid_fields_post_host(host)):
        pass
    else: 
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    cpu_obj = Cpu(host["cpu"]["cycles"], host["cpu"]["cores"], host["cpu"]["model"], host["cpu"]["architecture"], host["cpu"]["instruction_set"])
    host_obj = Host(host["memory"], host["storage"], host["ip_address"], host["hostname"], host["availability"], host["location"], cpu_obj.get_uuid())
    if(cpu_dao.insert_cpu(cpu_obj)):
        if(host_dao.insert_host(host_obj)): return host_obj.to_yaml(), 201
        else: 
            logs.logger.error(host_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(cpu_dao.get_msg())
        return "-1", 404

@host.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    host = json.loads(json.dumps(yaml.load(request.data)))["host"]
    host_obj = Host(host["memory"], host["storage"], host["ip_address"], host["hostname"], host["availability"], host["location"])
    host_obj.set_uuid(host["uuid"])
    cpu_obj = Cpu(host["cpu"]["cycles"], host["cpu"]["cores"], host["cpu"]["model"], host["cpu"]["architecture"], host["cpu"]["instruction_set"])
    if(host_dao.select_host(host_obj.get_uuid())):
        cpu_obj.set_uuid(host_dao.select_host(host_obj.get_uuid()).get_CPU_uuid())
        host_obj.set_CPU_uuid(cpu_obj.get_uuid())
        if(cpu_dao.update_cpu(cpu_obj)):
            if(host_dao.update_host(host_obj)): return host_obj.to_yaml(), 201
            else: 
                logs.logger.error(host_dao.get_msg())
                return "-1", 404
        else: 
            logs.logger.error(cpu_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(host_dao.get_msg())
        return "-1", 404
    

@host.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    host = json.loads(json.dumps(yaml.load(request.data)))["host"]
    id = None
    if(host["uuid"] != None): id = host["uuid"]
    host_obj = host_dao.select_host(host["uuid"])
    if(host_obj):
        if(host_dao.delete_host(id)):
            if(cpu_dao.delete_cpu(host_obj.get_CPU_uuid())): 
               logs.logger.info(host_dao.get_msg())
               return id, 200
            else: 
                logs.logger.error(cpu_dao.get_msg())
                return "-1", 404
        else: 
            logs.logger.error(host_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(host_dao.get_msg())
        return "-1", 404