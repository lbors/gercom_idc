import uuid
import json
import yaml
from model.slice_part import SlicePart
from dao.template_dao import TemplateDAO
from model.template import Template
template_dao = TemplateDAO()
class Vm(object):
    def __init__(self, name_yaml=None, name_hypervisor=None, description=None, memory=None, vcpu=None, 
                    storage=None, ip_address=None, slice_part_uuid=None, host_uuid=None, 
                    template_name=None, template_version=None, type=None):
        self.__uuid = str(uuid.uuid4())
        self.__memory = memory
        self.__vcpu = vcpu
        self.__storage = storage
        self.__ip_address = ip_address
        self.__name_hypervisor = name_hypervisor
        self.__slice_part_uuid = slice_part_uuid
        self.__host_uuid = host_uuid
        self.__template_name = template_name
        self.__template_version = template_version
        self.__name_yaml = name_yaml
        self.__description = description
        self.__type = type
        self.__template = Template()

    # Getters
    def get_uuid(self):
        return self.__uuid
    
    def get_memory(self):
        return self.__memory
    
    def get_vcpu(self):
        return self.__vcpu
    
    def get_storage(self):
        return self.__storage
    
    def get_ip_address(self):
        return self.__ip_address


    def get_name_hypervisor(self):
        return self.__name_hypervisor

    def get_slice_part_uuid(self):
        return self.__slice_part_uuid

    def get_host_uuid(self):
        return self.__host_uuid

    def get_template_name(self):
        return self.__template_name

    def get_template_version(self):
        return self.__template_version

    def get_name_yaml(self):
        return self.__name_yaml
    
    def get_description(self):
        return self.__description
    
    def get_template(self):
        return self.__template

    def get_type(self):
        return self.__type

    # Setters
    def set_uuid(self, uuid):
        self.__uuid = uuid

    def set_memory(self, memory):
        self.__memory = memory

    def set_vcpu(self, vcpu):
        self.__vcpu = vcpu

    def set_storage(self, storage):
        self.__storage = storage

    def set_ip_address(self, ip_address):
        self.__ip_address = ip_address

    def set_name_hypervisor(self, name_hypervisor):
        self.__name_hypervisor = name_hypervisor
    
    def set_slice_part_uuid(self, slice_part_uuid):
        self.__slice_part_uuid = slice_part_uuid
    
    def set_host_uuid(self, host_uuid):
        self.__host_uuid = host_uuid

    def set_template_name(self, template_name):
        self.__template_name = template_name

    def set_template_version(self, template_version):
        self.__template_version = template_version

    def set_template(self, template):
        self.__template = template

    def set_name_yaml(self, name_yaml):
        self.__name_yaml = name_yaml

    def set_description(self, description):
        self.__description = description

    def set_type(self, type):
        self.__type = type

    # Return a JSON of VM
    def to_json(self):
        return json.dumps(self.__dict__).replace("_Vm__", "")

    #Return a YAML of VM
    def to_yaml(self):
        return yaml.dump(self.__dict__).replace("_Vm__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "VM:\n    ").replace("}", "")

    # Prints VM
    def show(self):
        print(self.to_json())

    def to_object(self, vms_json):
        vms = []
        for vm in vms_json:
            vm_obj = Vm(vm["name"], None,  vm["description"], None, None, None,vm["ip"] ,
                None, None, vm["vdu-image"], None, vm["type"])
            template = template_dao.select_updated_template(vm["vdu-image"])
            
            if(template): 
                vm_obj.set_template_version(template.get_version())
            else: return 0
            vm_obj.set_template(template_dao.select_template_vm(vm_obj.get_template_name(), vm_obj.get_template_version()))
            vm_obj.set_memory(vm_obj.get_template().get_memory())
            vm_obj.set_vcpu(vm_obj.get_template().get_vcpu())
            vm_obj.set_storage(vm_obj.get_template().get_storage())
            name_hypervisor = vm_obj.get_name_yaml()
            vm_obj.set_name_hypervisor(name_hypervisor)
            if(vm.get("uuid")!=None): vm_obj.set_uuid(vm["uuid"])
            vms.append(vm_obj)
        return vms

    def __lt__(self, other):
        return self.get_type() < other.get_type()

    

    
