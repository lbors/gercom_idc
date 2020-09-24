from flask import Flask, Blueprint, request
from model.vim_type import VimType
from dao.vim_type_dao import VimTypeDAO
from slice_creator import logs
from ruamel.yaml import YAML
from slice_creator import validate_yaml
import json
import yaml

vim_type = Blueprint('vim_type', 'vim_type', url_prefix='/vim_type')
vim_type_dao = VimTypeDAO()

@vim_type.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim_type = json.loads(json.dumps(yaml.load(request.data)))["vim_type"]
    id = None
    if(vim_type["uuid"] != None): id = vim_type["uuid"]
    if(vim_type["name"] != None): id = vim_type["name"]
    # Get all VIM Type if uuid/name are null
    if(id == None):
        obj = VimType()
        result = vim_type_dao.select_all_vim_types()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_VimType__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Vim_Type:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get vim type")
            return "-1", 404
    # Get a Vim type
    vim_type = vim_type_dao.select_vim_type(id)
    if(vim_type): return vim_type.to_yaml(), 200
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@vim_type.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim_type = json.loads(json.dumps(yaml.load(request.data)))["vim_type"]
    if(validate_yaml.valid_fields_vim_type(vim_type)):
        pass
    else: 
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim_type_obj = VimType(vim_type["name"])
    vim_type_obj.show()
    if(vim_type_dao.insert_vim_type(vim_type_obj)): return vim_type_obj.to_yaml(), 201
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@vim_type.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim_type = json.loads(json.dumps(yaml.load(request.data)))["vim_type"]
    vim_type_obj = VimType(vim_type["name"])
    vim_type_obj.set_uuid(vim_type["uuid"])
    if(vim_type_dao.select_vim_type(vim_type_obj.get_uuid())):
        pass
    else:
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404
    if(vim_type_dao.update_vim_type(vim_type_obj)): return vim_type_obj.to_yaml(), 201
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@vim_type.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim_type = json.loads(json.dumps(yaml.load(request.data)))["vim_type"]
    id = None
    if(vim_type["uuid"] != None): id = vim_type["uuid"]
    if(vim_type_dao.select_vim_type(id)):
        pass
    else:
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404
    if(vim_type_dao.delete_vim_type(id)): 
        logs.logger.info(vim_type_dao.get_msg())
        return id, 200
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

