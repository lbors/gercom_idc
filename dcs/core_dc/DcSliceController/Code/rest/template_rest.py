from flask import Flask, Blueprint, request
from model.template import Template
from dao.template_dao import TemplateDAO
from dao.vim_type_dao import VimTypeDAO
from ruamel.yaml import YAML
from slice_creator import validate_yaml, logs
import json
import yaml

template = Blueprint('template', 'template', url_prefix='/template')
template_dao = TemplateDAO()
vim_type_dao = VimTypeDAO()

@template.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    template = json.loads(json.dumps(yaml.load(request.data)))["template"]
    id = None
    if(template["uuid"] != None): id = template["uuid"]
    if(template["VIM_Type_name"] != None): id = template["VIM_Type_name"]
    # Get all Templates if uuid are null
    if(id == None):
        obj = Template()
        result = template_dao.select_all_templates()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Template__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Template:\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get templates")
            return "-1", 404
    # Get a User
    template = template_dao.select_template(id)
    if(template): return template.to_yaml(), 200
    else: 
        logs.logger.error(template_dao.get_msg())
        return "-1", 404

@template.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    template = json.loads(json.dumps(yaml.load(request.data)))["template"]
    if(validate_yaml.valid_fields_post_template(template)):
        pass
    else: 
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    template_obj = Template(template["name"], template["version"], template["memory"], template["vcpu"], template["storage"], template["ip_address"], template["path"], template["VIM_Type_name"])
    if(vim_type_dao.select_vim_type(template["VIM_Type_name"])):
        if(template_dao.insert_template(template_obj)): return template_obj.to_yaml(), 201
        else: 
            logs.logger.error(template_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404
@template.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    template = json.loads(json.dumps(yaml.load(request.data)))["template"]
    template_obj = Template(template["VIM_Type_name"], template["version"], template["memory"], template["vcpu"], template["storage"], template["ip_address"], template["path"], template["VIM_Type_name"])
    template_obj.set_uuid(template["uuid"])
    if(template_dao.select_template(template_obj.get_uuid())):
        pass
    else:
        logs.logger.error(template_dao.get_msg())
        return "-1", 404
    if(vim_type_dao.select_vim_type(template["VIM_Type_name"])):
        if(template_dao.update_template(template_obj)): return template_obj.to_yaml(), 201
        else: 
            logs.logger.error(template_dao.get_msg())
            return "-1", 404
    else: 
        logs.logger.error(vim_type_dao.get_msg())
        return "-1", 404

@template.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    template = json.loads(json.dumps(yaml.load(request.data)))["template"]
    id = None
    if(template["uuid"] != None): id = template["uuid"]
    if(template_dao.select_template(id)):
        pass
    else:
        logs.logger.error(template_dao.get_msg())
        return "-1", 404
    if(template_dao.delete_template(id)): 
        logs.logger.info(template_dao.get_msg())
        return id, 200
    else: 
        logs.logger.error(template_dao.get_msg())
        return "-1", 404
