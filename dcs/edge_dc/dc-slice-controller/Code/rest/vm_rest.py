from flask import Flask, Blueprint, request
from model.vm import Vm
from dao.vm_dao import VmDAO
from dao.slice_part_dao import SlicePartDAO
from dao.template_dao import TemplateDAO
from slice_creator import logs
from slice_creator.handle_slice_creator import HandleSliceCreator
from ruamel.yaml import YAML
import json, yaml
from vm_factory import vm_manager
from slice_creator import logs

vm = Blueprint('vm', 'vm', url_prefix='/vm')
vm_dao = VmDAO()
template_dao = TemplateDAO()
slice_part_dao = SlicePartDAO()
slice_creator = HandleSliceCreator()


@vm.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vm = json.loads(json.dumps(yaml.load(request.data)))["vm"]
    id = None
    if(vm["uuid"] != None):
        id = vm["uuid"]
    # Get all Vms if uuid are null
    if(id == None):
        obj = Vm()
        result = vm_dao.select_all_vms()
        if(result):
            return yaml.dump([obj.__dict__ for obj in result]).replace("_Vm__", "").replace(", ", "\n    ").replace(",\n ", "\n   ").replace("{", "VM:\n    ").replace("}", "")
        else:
            logs.logger.error("Failed to get vms")
            return "-1", 404
    # Get a User
    vm = vm_dao.select_vm(id)
    if(vm):
        return vm.to_yaml(), 200
    else:
        logs.logger.error(vm_dao.get_msg())
        return "-1", 404


@vm.route('', methods=['POST'])
def post():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vms = []
    vm = json.loads(json.dumps(yaml.load(request.data)))["vdu"]
    vm_obj = Vm(vm["name"], None, vm["description"], None, None, None, vm["ip"],
                vm["slices"]["sliced"]["slice-part"][0]["dc-slice-part"], None, vm["vdu-image"], None, vm["type"])
    template = template_dao.select_updated_template(vm["vdu-image"])
    if(template): 
        vm_obj.set_template_version(template.get_version())
    else: 
        logs.logger.error(template_dao.get_msg())
        return "-1", 404

    vm_obj.set_template(template_dao.select_template_vm(vm_obj.get_template_name(), vm_obj.get_template_version()))
    vm_obj.set_memory(vm_obj.get_template().get_memory())
    vm_obj.set_vcpu(vm_obj.get_template().get_vcpu())
    vm_obj.set_storage(vm_obj.get_template().get_storage())
    vms.append(vm_obj)

    slice_part = slice_part_dao.select_slice_part(vm["slices"]["sliced"]["slice-part"][0]["dc-slice-part"])
    if(slice_part):
        if(template_dao.select_template(vm["vdu-image"])):
            vms = slice_creator.distribute_vms_insert(vms, slice_part.get_valid_from(), slice_part.get_valid_until())
            if(vms != 0):
                for row in vms:
                    if(vm_dao.insert_vm(row)):
                        pass
                    else:
                        logs.logger.error(vm_dao.get_msg())
                        return "-1", 404
                
                return vms[0].to_yaml(), 201
            else:
                logs.logger.error("Failed to insert VM")
                return "-1", 404
        else:
            logs.logger.error(template_dao.get_msg())
            return "-1", 404
    else:
        logs.logger.error(slice_part_dao.get_msg())
        return "-1", 404
          


@vm.route('', methods=['PUT'])
def put():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vms = []
    vm = json.loads(json.dumps(yaml.load(request.data)))["vdu"]
    if(vm_dao.select_vm(vm["uuid"])):
        pass
    else:
        logs.logger.error(vm_dao.get_msg())
        return "-1", 404
    vm_obj = Vm(vm["name"], None, vm["description"], None, None, None, vm["ip"],
                vm["slices"]["sliced"]["slice-part"][0]["dc-slice-part"], None, vm["vdu-image"], None, vm["type"])
    template = template_dao.select_updated_template(vm["vdu-image"])
    if(template): 
        vm_obj.set_template_version(template.get_version())
    else: 
        logs.logger.error(template_dao.get_msg())
        return "-1", 404

    vm_obj.set_template(template_dao.select_template_vm(vm_obj.get_template_name(), vm_obj.get_template_version()))
    vm_obj.set_memory(vm_obj.get_template().get_memory())
    vm_obj.set_vcpu(vm_obj.get_template().get_vcpu())
    vm_obj.set_storage(vm_obj.get_template().get_storage())
    vm_obj.set_uuid(vm["uuid"])
    vms.append(vm_obj)
    slice_part = slice_part_dao.select_slice_part(vm["slices"]["sliced"]["slice-part"][0]["dc-slice-part"])
    if(slice_part):
        if(template_dao.select_template(vm["vdu-image"])):
            vms = slice_creator.distribute_vms_update(vms, slice_part.get_valid_from(), slice_part.get_valid_until())
            if(vms != 0):
                for row in vms:
                    if(vm_dao.update_vm(row)):
                        pass
                    else: 
                        logs.logger.error(vm_dao.get_msg())
                        return "-1", 404
                    return vms[0].to_yaml(), 201
            else:
                logs.logger.error("Failed to update VM")
                return "-1", 404
        else:
            logs.logger.error("Template selected does not exist")
            return f"-1", 404
    else:
        logs.logger.error("Slice Part selected does not exist")
        return f"-1", 404


@vm.route('', methods=['DELETE'])
def delete():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    vm = json.loads(json.dumps(yaml.load(request.data)))["vm"]
    id = None
    if(vm["uuid"] != None):
        id = vm["uuid"]
    if(vm_dao.select_vm(id)):
        pass
    else:
        logs.logger.error(vm_dao.get_msg())
        return "-1", 404
    if(vm_dao.delete_vm(id)):
        logs.logger.info(vm_dao.get_msg())
        return id, 200
    else:
        logs.logger.error(vm_dao.get_msg())
        return "-1", 404

@vm.route('start/<string:vm_name>', methods=['GET'])
def start_vm(vm_name):
    #start vm
    if(vm_manager.start_vm(vm_name)):
        return logs.callback(1, f"VM {vm_name} started"), 201
    else:
        return logs.callback(0, f"VM {vm_name} not found"), 404

@vm.route('shutdown/<string:vm_name>', methods=['GET'])
def shutdown_vm(vm_name):
    #start vm
    if(vm_manager.shutdown_vm(vm_name)):
        return logs.callback(1, f"Done."), 201
    else:
        return logs.callback(0, f"VM {vm_name} not found"), 404

@vm.route('list_all', methods=['GET'])
def list_all():
    #list all vms
    vms = vm_manager.list_all_vms()
    if(vms):
        return logs.callback(1, vms), 201
    else:
        return logs.callback(0, f"Error"), 404

@vm.route('destroy/<string:vm_name>', methods=['GET'])
def destroy_vm(vm_name):
    #start vm
    if(vm_manager.destroy_vm(vm_name)):
        return logs.callback(1, f"Done."), 201
    else:
        return logs.callback(0, f"VM {vm_name} not found"), 404

@vm.route('delete/<string:vm_name>', methods=['GET'])
def delete_vm(vm_name):
    #start vm
    if(vm_manager.delete_vm(vm_name)):
        vm_dao.delete_vm_by_name(vm_name)
        return logs.callback(1, f"Done."), 201
    else:
        return logs.callback(0, f"VM {vm_name} not found"), 404

@vm.route('state/<string:vm_name>', methods=['GET'])
def state_vm(vm_name):
    #start vm
    if(vm_manager.get_vm_state(vm_name)):
        return logs.callback(1, f"Done."), 201
    else:
        return logs.callback(0, f"VM {vm_name} not found"), 404