from flask import Flask, Blueprint, request
from model.user import User
from dao.user_dao import UserDAO
from ruamel.yaml import YAML
from slice_creator import validate_yaml, logs
import json
import yaml

user = Blueprint('user', 'user', url_prefix='/user')
user_dao = UserDAO()

@user.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    user = json.loads(json.dumps(yaml.load(request.data)))["user"]
    id = None
    if(user["uuid"] != None): id = user["uuid"]
    # Get all Users if uuid/login are null
    if(id == None):
        obj = User()
        result = user_dao.select_all_users()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_User__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "User:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get users")
            return "-1", 404
    # Get a User
    user = user_dao.select_user(id)
    if(user): return user.to_yaml(), 200
    else: 
        logs.logger.error(user_dao.get_msg())
        return "-1", 404

@user.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    user = json.loads(json.dumps(yaml.load(request.data)))["user"]
    if(validate_yaml.valid_fields_post_user(user)):
        pass
    else: 
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    user_obj = User(user["name"])
    if(user_dao.insert_user(user_obj)): return user_obj.to_yaml(), 201
    else: 
        logs.logger.error(user_dao.get_msg())
        return "-1", 404

@user.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    user = json.loads(json.dumps(yaml.load(request.data)))["user"]
    user_obj = User(user["name"])
    user_obj.set_uuid(user["uuid"])
    if(user_dao.select_user(user_obj.get_uuid())):
        pass
    else:
        logs.logger.error(user_dao.get_msg())
        return "-1", 404
    if(user_dao.update_user(user_obj)): return user_obj.to_yaml(), 201
    else: 
        logs.logger.error(user_dao.get_msg())
        return "-1", 404

@user.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    user = json.loads(json.dumps(yaml.load(request.data)))["user"]
    id = None
    if(user["name"] != None): id = user["name"]
    if(user_dao.select_user(id)):
        pass
    else:
        logs.logger.error(user_dao.get_msg())
        return "-1", 404
    if(user_dao.delete_user(id)): 
        logs.logger.info(user_dao.get_msg())
        return id, 200
    else: 
        logs.logger.error(user_dao.get_msg())
        return "-1", 404