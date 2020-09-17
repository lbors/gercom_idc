import uuid
import json
import yaml

class Cpu():

    def __init__(self, cycles=None, cores=None, model=None, architecture=None, instruction_set=None):
        self.__uuid = str(uuid.uuid4())
        self.__cycles = cycles 
        self.__cores = cores
        self.__model = model
        self.__architecture = architecture
        self.__instruction_set = instruction_set

   # Getters
    def get_uuid(self):
        return self.__uuid

    def get_cycles(self):
        return self.__cycles

    def get_cores(self):
        return self.__cores

    def get_model(self):
        return self.__model
    
    def get_architecture(self):
        return self.__architecture

    def get_instruction_set(self):
        return self.__instruction_set

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid
    
    def set_cycles(self, cycles):
        self.__cycles = cycles

    def set_cores(self, cores):
        self.__cores = cores

    def set_model(self, model):
        self.__model = model

    def set_architecture(self, architecture):
        self.__architecture = architecture

    def set_instruction_set(self, instruction_set):
        self.__instruction_set = instruction_set
    

    # Return a JSON of VIM
    def to_json(self):
        return json.dumps(self.__dict__)

    #Return a YAML of VM
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Cpu__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "CPU:\n    ").replace("}", "")


    # Prints VIM
    def show(self):
        print(self.to_json().replace("_Cpu__", ""))

