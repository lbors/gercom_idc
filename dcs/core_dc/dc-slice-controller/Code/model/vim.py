import uuid
import json
import yaml
from model.vim_type import VimType

class Vim():

    def __init__(self, dashboard_user=None, dashboard_password=None, vim_type_name=None):
        self.__uuid = str(uuid.uuid4())
        self.__dashboard_user = dashboard_user 
        self.__dashboard_password = dashboard_password
        self.__vim_type_name = vim_type_name

   # Getters
    def get_uuid(self):
        return self.__uuid

    def get_dashboard_user(self):
        return self.__dashboard_user

    def get_dashboard_password(self):
        return self.__dashboard_password

    def get_vim_type_name(self):
        return self.__vim_type_name

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid
    
    def set_dashboard_user(self, dashboard_user):
        self.__dashboard_user = dashboard_user

    def set_dashboard_password(self, dashboard_password):
        self.__dashboard_password = dashboard_password

    def set_vim_type_name(self, vim_type_name):
        self.__vim_type_name = vim_type_name

    # Return a JSON of VIM
    def to_json(self):
        return json.dumps(self.__dict__)

    #Return a YAML of Vim
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Vim__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Vim:\n    ").replace("}", "")


    # Prints VIM
    def show(self):
        print(self.to_json().replace("_Vim__", ""))

