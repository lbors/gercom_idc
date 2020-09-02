from flask import Flask, Blueprint, request
from model.vim import Vim
from dao.vim_dao import VimDAO
from dao.vim_type_dao import VimTypeDAO
from slice_creator import logs
from ruamel.yaml import YAML
import json
import yaml

vim = Blueprint('vim', 'vim', url_prefix='/vim')
vim_dao = VimDAO()
vim_type_dao = VimTypeDAO()

@vim.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim = json.loads(json.dumps(yaml.load(request.data)))["vim"]
    id = None
    if(vim["uuid"] != None): id = vim["uuid"]
    # Get all Vims if uuid are null
    if(id == None):
        obj = Vim()
        result = vim_dao.select_all_vims()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Vim__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Vim:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get vims")
            return "-1", 404
    # Get a User
    vim = vim_dao.select_vim(id)
    if(vim): return vim.to_yaml(), 200
    else: 
        logs.logger.error(vim_dao.get_msg())
        return "-1", 404

@vim.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim = json.loads(json.dumps(yaml.load(request.data)))["vim"]
    vim_obj = Vim(None, None, vim["name"])
    if(vim_type_dao.select_vim_type(vim["name"])):
        if(vim_dao.insert_vim(vim_obj)): return vim_obj.to_yaml(), 201
        else: 
            logs.logger.error(vim_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@vim.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim = json.loads(json.dumps(yaml.load(request.data)))["vim"]
    vim_obj = Vim(None, None, vim["name"])
    vim_obj.set_uuid(vim["uuid"])
    if(vim_dao.select_vim(vim_obj.get_uuid())):
        pass
    else:
        logs.logger.error(vim_dao.get_msg())
        return "-1", 404
    if(vim_type_dao.select_vim_type(vim["name"])):
        if(vim_dao.update_vim(vim_obj)): return vim_obj.to_yaml(), 201
        else: 
            logs.logger.error(vim_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@vim.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vim = json.loads(json.dumps(yaml.load(request.data)))["vim"]
    id = None
    if(vim["uuid"] != None): id = vim["uuid"]
    if(vim_dao.select_vim(id)):
        pass
    else:
        logs.logger.error(vim_dao.get_msg())
        return "-1", 404
    if(vim_dao.delete_vim(id)): 
        logs.logger.info(vim_dao.get_msg())
        return id, 200
    else: 
        logs.logger.error(vim_dao.get_msg())
        return "-1", 404