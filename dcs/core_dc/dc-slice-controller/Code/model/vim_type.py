import uuid
import json
import yaml

class VimType():
    
    def __init__(self, name=None):
        self.__uuid = str(uuid.uuid4())
        self.__name = name

    # Getters
    def get_uuid(self):
        return self.__uuid

    def get_name(self):
        return self.__name

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid

    def set_name(self, name):
        self.__name = name

    # Return a JSON of VimType
    def to_json(self):
        return json.dumps(self.__dict__)

    #Return a YAML of Vim Type
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_VimType__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Vim_Type:\n    ").replace("}", "")


    # Prints VimType
    def show(self):
        print(self.to_json().replace("_VimType__", ""))