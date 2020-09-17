import uuid
import json
import yaml
from model.vim_type import VimType

class Template(object):

    def __init__(self, name=None, version=None, memory=None, vcpu=None, storage=None, ip_address=None, path=None, vim_type_name=None):
        self.__uuid = str(uuid.uuid4())
        self.__name = name
        self.__version = version
        self.__memory = memory
        self.__vcpu = vcpu
        self.__storage = storage
        self.__ip_address = ip_address
        self.__path = path
        self.__vim_type_name = vim_type_name

    # Getters
    def get_uuid(self):
        return self.__uuid

    def get_name(self):
        return self.__name

    def get_version(self):
        return self.__version
    
    def get_memory(self):
        return self.__memory
    
    def get_vcpu(self):
        return self.__vcpu
    
    def get_storage(self):
        return self.__storage
    
    def get_ip_address(self):
        return self.__ip_address

    def get_path(self):
        return self.__path

    def get_vim_type_name(self):
        return self.__vim_type_name

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid

    def set_name(self, name):
        self.__name = name

    def set_version(self, version):
        self.__version = version

    def set_memory(self, memory):
        self.__memory = memory

    def set_vcpu(self, vcpu):
        self.__vcpu = vcpu

    def set_storage(self, storage):
        self.__storage = storage

    def set_ip_address(self, ip_address):
        self.__ip_address = ip_address
    
    def set_path(self, path):
        self.__path = path
    
    def set_vim_type_name(self, vim_type_name):
        self.__vim_type_name = vim_type_name
    
    # Return a JSON of VM
    def to_json(self):
        return json.dumps(self.__dict__)

    #Return a YAML of Template
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Template__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Template:\n    ").replace("}", "")


    # Prints VM
    def show(self):
        print(self.to_json().replace("_Template__", ""))