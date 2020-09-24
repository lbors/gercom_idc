import uuid
import json
import yaml

class Host(object):
    
    def __init__(self, memory=None, storage=None, ip_address=None, hostname=None, availability=None, location=None, CPU_uuid=None):
        self.__uuid = str(uuid.uuid4())
        self.__memory = memory
        self.__storage = storage
        self.__ip_address = ip_address
        self.__hostname = hostname
        self.__availability = availability
        self.__location = location
        self.__CPU_uuid = CPU_uuid

    # Getters
    def get_uuid(self):
        return self.__uuid
    
    def get_memory(self):
        return self.__memory
    
    def get_storage(self):
        return self.__storage
    
    def get_ip_address(self):
        return self.__ip_address

    def get_hostname(self):
        return self.__hostname

    def get_availability(self):
        return self.__availability
    
    def get_location(self):
        return self.__location
    
    def get_CPU_uuid(self):
        return self.__CPU_uuid

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid

    def set_memory(self, memory):
        self.__memory = memory

    def set_storage(self, storage):
        self.__storage = storage

    def set_ip_address(self, ip_address):
        self.__ip_address = ip_address

    def set_hostname(self, hostname):
        self.__hostname = hostname

    def set_availability(self, availability):
        self.__availability = availability

    def set_location(self, location):
        self.__location = location
    
    def set_CPU_uuid(self, CPU_uuid):
        self.__CPU_uuid = CPU_uuid
    
    # Return a JSON of host
    def to_json(self):
        return json.dumps(self.__dict__)

    #Return a YAML of Host
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Host__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "Host:\n    ").replace("}", "")


    # Prints user
    def show(self):
        print(self.to_json().replace("_Host__", ""))
