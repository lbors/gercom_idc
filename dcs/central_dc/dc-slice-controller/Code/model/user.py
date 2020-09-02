import uuid
import json
import yaml

class User():
    
    def __init__(self, name=None):
        self.__uuid = str(str(uuid.uuid4()))
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
    
    # Return a JSON of user
    def to_json(self):
        return json.loads(json.dumps(self.__dict__).replace("_User__", ""))

    #Return a YAML of User
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_User__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "User:\n    ").replace("}", "")

    # Prints user
    def show(self):
        print(self.to_json())