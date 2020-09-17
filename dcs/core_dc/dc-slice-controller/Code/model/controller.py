import json
import yaml

class Controller():

    def __init__(self, id=None, role=None, configuration_protocol=None, configuration_interface=None, provider=None):
        self.__provider = provider
        self.__id = id
        self.__role = role 
        self.__configuration_protocol = configuration_protocol
        self.__configuration_interface = configuration_interface

   # Getters
    def get_id(self):
        return self.__id

    def get_role(self):
        return self.__role

    def get_configuration_protocol(self):
        return self.__configuration_protocol

    def get_configuration_interface(self):
        return self.__configuration_interface
    
    def get_provider(self):
        return self.__provider
    
    # Setters
    def set_id(self, id):
        self.__id = id
    
    def set_role(self, role):
        self.__role = role

    def set_configuration_protocol(self, configuration_protocol):
        self.__configuration_protocol = configuration_protocol

    def set_configuration_interface(self, configuration_interface):
        self.__configuration_interface = configuration_interface

    def set_provider(self, provider):
        self.__provider = provider

     # Return a JSON of VIM
    def to_json(self):
        return json.dumps(self.__dict__)
    
    #Return a YAML of Controller
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Controller__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Controller:\n    ").replace("}", "")

    # Prints VIM
    def show(self):
        print(self.to_json().replace("_Controller__", ""))

