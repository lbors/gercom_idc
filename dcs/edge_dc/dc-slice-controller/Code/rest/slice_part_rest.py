from slice_creator import logs
from flask import Flask, Blueprint, request
from model.slice_part import SlicePart
from model.vm import Vm
from model.vim import Vim
from model.controller import Controller
from dao.slice_part_dao import SlicePartDAO
from dao.vm_dao import VmDAO
from dao.vim_dao import VimDAO
from dao.user_dao import UserDAO
from dao.controller_dao import ControllerDAO
from dao.update_slice_part_dao import UpdateSlicePartDAO
from dao.vim_type_dao import VimTypeDAO
from slice_creator.handle_slice_creator import HandleSliceCreator
from slice_creator import validate_yaml
from vm_factory import slice_part_instantiation, elasticity
import json, yaml, logging, _thread, time
from datetime import datetime
from vm_factory import slice_part_instantiation

slice_part = Blueprint('slice_part', 'slice_part', url_prefix='/slice_part')
slice_creator = HandleSliceCreator()
slice_part_dao = SlicePartDAO()
controller_dao = ControllerDAO()
vm_dao = VmDAO()
vim_dao = VimDAO()
vim_type_dao = VimTypeDAO()
user_dao = UserDAO()
update_slice_dao = UpdateSlicePartDAO()
vm = Vm()

@slice_part.route('', methods=['GET'])
def get():
    try:
        yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404

    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]
    id = None
    if(slice_part["uuid"] != None): id = slice_part["uuid"]
    # Get all Slice Part if uuid are null
    if(id == None):
        obj = SlicePart()
        result = slice_part_dao.select_all_slice_parts()
        if(result): 
            return yaml.dump([obj.__dict__ for obj in result]).replace("_SlicePart__", "").replace("!!python/object:model.vm.Vm", "").replace("{", "VM:\n    ").replace("_Vm__", "").replace(", ", ",\n    ").replace("}", "")

        else: 
            logs.logger.error("Failed to get slice part")
            return "-1", 404
    # Get a User
    slice_part = slice_part_dao.select_slice_part(id)
    if(slice_part): return yaml.dump(slice_part), 200 
    else: 
        logs.logger.error(slice_part_dao.get_msg())
        return logs.callback(0, slice_part_dao.get_msg()), 404

@slice_part.route('request', methods=['POST'])
def post():
    # validate yaml
    request_ts = time.time() # start
    try:
            yaml.load(request.data)
    except Exception as e:
        logs.logger.error("Invalid yaml file!")
        return logs.callback(0, "Invalid yaml file!"), 404

    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]
    # validate yaml
    controller = controller_dao.select_all_controllers()
    if(controller == 0 ):
        logs.logger.error("No registered controller")
        return logs.callback(0, "No registered controller"), 404
    #instantiating vim_obj, slice_part_obj
    vim_obj = Vim(None, None, slice_part["VIM"]["name"])
    monitoring = slice_part["monitoring-parameters"]["tool"]
    logs.logger.info("MONITORING = " + monitoring)
    status = 'accepted'
    slice_part_obj = SlicePart(slice_part["name"] + "-" + monitoring, None, None, "DC",  vim_obj.get_uuid(), controller.get_id() , None, None, status)
    # converting json array from vms to array object from vms
    logs.logger.info(slice_part["VIM"]["vdus"])
    slice_part_vms = vm.to_object(slice_part["VIM"]["vdus"])
    if(slice_part_vms):
        pass
    else:
        logs.logger.error("Template selected does not exist")
        return logs.callback(0, "Template selected does not exist"), 404
    # verifying whether vms can be allocated (vms = 0 if vms can not be allocated)
    vms = slice_creator.distribute_vms_insert(slice_part_vms, slice_part_obj.get_valid_from(), slice_part_obj.get_valid_until())
    if(vim_type_dao.select_vim_type(slice_part["VIM"]["name"])):
        pass
    else:
        logs.logger.error("VIM Type selected does not exist")
        return logs.callback(0, "VIM Type selected does not exist"), 404
    if(vms != 0):
        if(vim_dao.insert_vim(vim_obj)):
            if(slice_part_dao.insert_slice_part(slice_part_obj)): 
                # set uuid with new Auto Increment ID
                new_id = slice_part_dao.select_last(slice_part_obj.get_user())
                slice_part_obj.set_uuid(new_id)
                logs.logger.info("slice_part_id = " + str(slice_part_obj.get_uuid()))
                # mudou
                for row in vms:
                    # change name_hypervisor
                    new_name_hypervisor = row.get_name_hypervisor()+'-'+slice_part_obj.get_controller_id()+'-'+ str(new_id)
                    row.set_name_hypervisor(new_name_hypervisor)
                    row.set_slice_part_uuid(new_id)
                    if(vm_dao.insert_vm(row)):
                        slice_part_obj.set_vms(slice_part_dao.select_slice_part_vms(slice_part_obj.get_uuid()))
                    else:
                        msg = vm_dao.get_msg()
                        vm_dao.truncate_vm(slice_part_obj.get_uuid())
                        slice_part_dao.delete_slice_part(slice_part_obj.get_uuid())
                        vim_dao.delete_vim(vim_obj.get_uuid())
                        logs.logger.error(msg)
                        return logs.callback(0, msg), 404
                # Success
                response = {"slice-part-id": {"dc-slice-controller-id": str(slice_part_obj.get_controller_id()), "id": str(slice_part_obj.get_uuid())} }
                request_te = time.time()
                logs.echo_to_file("Slice Part " + str(slice_part_obj.get_controller_id()) + " request executed in "+ str(request_te - request_ts)+" seconds")
                return logs.callback(1, response), 201
            else: 
                vim_dao.delete_vim(vim_obj.get_uuid())
                logs.logger.error(slice_part_dao.get_msg())
                return "-1", 404
        else: 
            logs.logger.error(vim_dao.get_msg())
            return "-1", 404
    logs.logger.error("No resources available to insert VMS")
    return "-1", 404
    
 
@slice_part.route('', methods=['PUT'])
def put():
    try:
            yaml.load(request.data)
    except Exception as e:
        logs.logger.error(e)
        logs.logger.error("Invalid yaml file!")
        return "-1", 404
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]
    #looking for controller id
    controller = controller_dao.select_all_controllers()
    if(controller == 0 ):
        logs.logger.error("No registered controller")
        return "-1", 404

    if(vim_dao.select_vim(slice_part["vim_uuid"]) == 0):
        logs.logger.error(vim_dao.get_msg())
        return "-1", 404
    slice_part_obj = SlicePart(slice_part["name"], None, None, "DC", slice_part["vim_uuid"], controller.get_id(), None, None)
    slice_part_obj.set_uuid(slice_part["uuid"])
    old_slice_part = slice_part_dao.select_slice_part(slice_part_obj.get_uuid())
    if(old_slice_part == 0):
        logs.logger.error(slice_part_dao.get_msg())
        return "-1", 404
    slice_part_obj.set_status(old_slice_part.get_status())
    if(slice_part_dao.update_slice_part(slice_part_obj)):
        return str(slice_part_obj.get_uuid()), 201
    else: 
        logs.logger.error(update_slice_dao.get_msg())
        return "-1", 404
    
@slice_part.route('', methods=['DELETE'])
def delete():
    # parse to object
    search_slice_ts = time.time()
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part-id"]
    slice_part_obj = slice_part_dao.select_slice_part(slice_part["uuid"])
    search_slice_te = time.time()
    logs.echo_to_file(f"------------- Delete Slice Part " + str(slice_part["uuid"]) + " -------------")
    logs.echo_to_file("Fetched slice in database in " + str( search_slice_te - search_slice_ts ) + " seconds")
    # search slice_part
    if(str(slice_part_obj.get_uuid())):
        # delete hypervisor_vms
        if(slice_part_instantiation.delete_slice_part_vms(slice_part_obj) == 0):
            return logs.callback(0, "Failed to delete hypervisors"), 404
        # delete slice_part of database
        if(vm_dao.truncate_vm(slice_part_obj.get_uuid())):
            if(slice_part_dao.delete_slice_part(slice_part_obj.get_uuid())): 
                if(vim_dao.delete_vim(slice_part_obj.get_vim_uuid())):
                    total_time = time.time()
                    logs.echo_to_file(f"Deleted Slice Part in " + str(total_time - search_slice_ts) + " seconds") #log
                    return logs.callback(1, f"Successfully removed slice_part = {slice_part_obj.get_uuid()}"), 201
                else: 
                    logs.logger.error(vim_dao.get_msg())
                    return logs.callback(0, "Failed to delete slice_part information"), 404
            else: 
                logs.logger.error(slice_part_dao.get_msg())
                return logs.callback(0, "Failed to get slice_part"), 404
        else: 
            logs.logger.error(vm_dao.get_msg())
            return logs.callback(0, "Failed to delete slice_part information"), 404
    else: 
        logs.logger.error(slice_part_dao.get_msg())
        return logs.callback(0, "Failed to get slice_part"), 404


@slice_part.route('activate', methods=['POST'])
def activate():
    # parse to object
    search_slice_ts = time.time() # start time
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part-id"]
    slice_part_obj = slice_part_dao.select_slice_part(slice_part["uuid"])
    search_slice_te = time.time() # end time
    # start logging time
    logs.echo_to_file(f"------------- Activate Slice Part " + str(slice_part["uuid"]) + " -------------")
    logs.echo_to_file("Fetched slice in database in " + str( search_slice_te - search_slice_ts ) + " seconds")
    # search slice_part
    if(str(slice_part_obj.get_uuid())):
        #deploy 
        result = slice_part_instantiation.deploy_slice_part(slice_part_obj)
        if (result == 0): return logs.callback(0, "Error"), 404
        response = {"slice-part-id": {"dc-slice-controller-id": slice_part_obj.get_controller_id(), "id": slice_part_obj.get_uuid()}}
        response.update(result)
        total_time = time.time()
        logs.echo_to_file("Executed in " + str(total_time - search_slice_ts) + " seconds")
        return logs.callback(1, response), 201
    else:
        logs.echo_to_file("Executed in " + str(total_time - search_slice_ts) + " seconds")
        return logs.callback(0, "Error"), 404

@slice_part.route('stop', methods=['POST'])
def stop():
    # parse to object
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part-id"]
    slice_part_obj = slice_part_dao.select_slice_part(slice_part["uuid"])
    # search slice_part
    if(slice_part_obj.get_uuid()):
        #deploy 
        if (slice_part_instantiation.stop_slice_part_vms(slice_part_obj) != 1): return "-1", 404
        return str(slice_part_obj.get_uuid()), 201
    else: return "-1", 404

@slice_part.route('start', methods=['POST'])
def start():
    # parse to object
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["slices"]["sliced"]["slice-part-id"]
    slice_part_obj = slice_part_dao.select_slice_part(slice_part["uuid"])
    # search slice_part
    if(slice_part_obj.get_uuid()):
        if (slice_part_instantiation.start_slice_part_vms(slice_part_obj) != 1): return "-1", 404
        return str(slice_part_obj.get_uuid()), 201
    else: return "-1", 404


@slice_part.route('add_vm', methods=['POST'])
def add_vm():
    #return logs.callback(0, "Could not be created due to insufficient resources"), 503
    # parse to object
    slice_part = json.loads(json.dumps(yaml.load(request.data)))["elasticity"]["slice"]["slice-parts"][0]["dc-slice-part"]
    slice_part_obj = slice_part_dao.select_slice_part(slice_part["dc-slice-part-id"]["slice-part-uuid"])
    # get vms yaml
    vms_yaml = slice_part["VIM"]["vdus"]
    vms_to_obj = []
    for vm_yaml in vms_yaml:
        vms_to_obj.append(vm_yaml["vdu"])
    # get vms object
    slice_part_vms = vm.to_object(vms_to_obj)
    if(not slice_part_vms):
        logs.logger.error("Template selected does not exist")
        return logs.callback(0, "Template selected does not exist"), 404
    # verifying whether vms can be allocated (vms = 0 if vms can not be allocated)
    vms = slice_creator.distribute_vms_insert(slice_part_vms, slice_part_obj.get_valid_from(), slice_part_obj.get_valid_until())
    if(not vim_type_dao.select_vim_type(slice_part["VIM"]["name"])):
        logs.logger.error("VIM Type selected does not exist")
        return logs.callback(0, "VIM Type selected does not exist"), 404
    for row in vms:
        # change name_hypervisor
        new_name_hypervisor = row.get_name_hypervisor()
        row.set_name_hypervisor(new_name_hypervisor)
        row.set_slice_part_uuid(slice_part_obj.get_uuid())
        if(not vm_dao.insert_vm(row)):
            msg = vm_dao.get_msg()
            logs.logger.error(msg)
            return logs.callback(0, msg), 404
    
    # deploy new vm
    bridge  = slice_part["VIM"]["vswitch"]
    master_entrypoints = slice_part["VIM"]["vim-ref"]
    master_credentials = slice_part["VIM"]["vim-credential"]
    logs.logger.info(f"bridge = {bridge}, master_entrypoints = {master_entrypoints}, master_credentials = {master_credentials}")

    result = elasticity.update_slice_part(slice_part_obj, vms, bridge, master_entrypoints, master_credentials)
    if(result==0): return logs.callback(0, "Error"), 404
    else:
        response = json.loads(json.dumps(yaml.load(request.data)))["elasticity"]
        response.pop("type")
        response["slice"]["slice-parts"][0]["dc-slice-part"]["VIM"].pop("vim-ref")
        response["slice"]["slice-parts"][0]["dc-slice-part"]["VIM"].pop("vim-credential")
        response["slice"]["slice-parts"][0]["dc-slice-part"]["VIM"].pop("vswitch")
        # return response
        return logs.callback(1, response), 201
        

@slice_part.route('test', methods=['GET'])
def test():
    result = {"monitoring-handle":{"ip": "i", "port": "p"}, "vim-handle":{"ip": "i", "port": "p"}, "ssh-handle":{"ip": "i", "port": "p"}}
    response = {"slice-part-id": {"dc-slice-controller-id": "test", "id": "test"}}
    response.update(result)
    return logs.callback(1, response), 201
