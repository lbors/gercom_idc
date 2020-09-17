import uuid
import json
import yaml
import time # Not implemented yet
from model.user import User
from model.vim import Vim

class SlicePart():
    
    def __init__(self, name=None, valid_from=None, valid_until=None, user=None, vim_uuid=None, controller_id=None, ip=None, port= None, status="accepted"):
        self.__uuid = ''
        self.__valid_from = valid_from
        self.__valid_until = valid_until
        self.__user = user
        self.__vim_uuid = vim_uuid
        self.__vms = []
        self.__controller_id = controller_id
        self.__ip = ip
        self.__port = port
        self.__name = name
        self.__status = status

    # Getters
    def get_uuid(self):
        return self.__uuid

    def get_valid_from(self):
        return self.__valid_from

    def get_valid_until(self):
        return self.__valid_until

    def get_user(self):
        return self.__user

    def get_vim_uuid(self):
        return self.__vim_uuid
    
    def get_vms(self):
        return self.__vms

    def get_controller_id(self):
        return self.__controller_id

    def get_status(self):
        return self.__status

    def get_ip(self):
        return self.__ip

    def get_port(self):
        return self.__port

    def get_name(self):
        return self.__name

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid

    def set_status(self, status):
        self.__status = status

    def set_valid_from(self, valid_from):
        self.__valid_from = valid_from

    def set_valid_until(self, valid_until):
        self.__valid_until = valid_until

    def set_user(self, user):
        self.__user = user
    
    def set_vim_uuid(self, vim_uuid):
        self.__vim_uuid = vim_uuid

    def set_vms(self, vms):
        self.__vms = vms

    def set_controller_id(self, controller_id):
        self.__controller_id = controller_id

    def set_ip(self, ip):
        self.__ip = ip
    
    def set_port(self, port):
        self.__port = port

    def set_name(self, name):
        self.__name = name

    # Return a JSON of Slice_part
    def to_json(self):
        return json.dumps(self.__dict__).replace("_SlicePart__", "")

    #Return a YAML of Slice_part
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_SlicePart__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("}", "").replace("_Vm__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "VM:\n    ").replace("}", "").replace("_Template__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Template:\n    ").replace("}", "")

   
    # Prints Slice
    def show(self):
        print(self.to_json())
    