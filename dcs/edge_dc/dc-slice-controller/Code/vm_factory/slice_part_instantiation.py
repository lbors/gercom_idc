from model.slice_part import SlicePart
from dao.slice_part_dao import SlicePartDAO
from dao.vim_dao import VimDAO
from vm_factory import vm_manager
from vm_factory import kubernetes, xen
from slice_creator import logs
from network_manager import network_manager
import time

# --------- STATUS LIST ---------
# accepted -> not deployed 
# ready -> deployed
# active -> active
# configuring 
# failed -> error
# removing 
# starting
# stopping
# deleted

def prepare_slice_part_vms(slice_part):
    vms = slice_part.get_vms()
    # preparing vm clone
    logs.logger.info("Creating slice_part vms")
    slice_part.set_status("configuring")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    clone_all_ts = time.time() # start
    for vm in vms:
        clone_individual_ts = time.time() #start
        if(vm_manager.clone_vm(vm.get_template_name(), vm.get_name_hypervisor()) == 0):
            logs.logger.info("Stopping vm clone. An error has occurred")
            return 0
        clone_individual_te = time.time() # end
        logs.echo_to_file(f"Cloned VM " + vm.get_name_hypervisor() + " in " + str(clone_individual_te - clone_individual_ts) + " seconds") #log
    clone_all_te = time.time() # end
    logs.echo_to_file(f"Cloned all VMs in " + str(clone_all_te - clone_all_ts) + " seconds") #log
    return 1

def delete_slice_part_vms(slice_part):
    # Check status
    # status = slice_part.get_status()
    # if(status == 'active'):
    #     logs.logger.info(f"Could not delete slice_part because it is {status}")
    #     return 0
    slice_part.set_status("removing")
    vms = slice_part.get_vms()
    del_all_ts = time.time() # start
    #preparing vm delete
    for vm in vms:
        del_individual_ts = time.time() #start
        if(vm_manager.vm_exists(vm.get_name_hypervisor())):
            vm_manager.delete_vm(vm.get_name_hypervisor())
        del_individual_te = time.time() # end
        logs.echo_to_file(f"Deleted VM " + vm.get_name_hypervisor() + " in " + str(del_individual_te - del_individual_ts) + " seconds") #log
    del_all_te = time.time() # end
    logs.echo_to_file(f"Deleted all VMs in " + str(del_all_te - del_all_ts) + " seconds") #log
    # delete iptables
    del_entry_ts = time.time()
    kubernetes.delete_entrypoints(slice_part)
    del_entry_te = time.time()
    logs.echo_to_file(f"Deleted entrypoints in " + str(del_entry_te - del_entry_ts) + " seconds") #log
    # delete bridge
    del_bridge_ts = time.time()
    network_manager.remove_resource(slice_part.get_uuid())
    del_bridge_te = time.time()
    logs.echo_to_file(f"Deleted bridge in " + str(del_bridge_te - del_bridge_ts) + " seconds") #log
    # update status
    slice_part.set_status("deleted")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    return 1
    
def start_slice_part_vms(slice_part):
    # Check status
    status = slice_part.get_status()
    #if(status != 'ready'):
    #    logs.logger.info(f"Could not start slice_part because it is not ready. Slice_part status = {status}")
    #    return 0
    slice_part.set_status("starting")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    vms = slice_part.get_vms()
    # preparing vm start
    logs.logger.info("Starting slice_part vms")
    for vm in vms:
        if(vm_manager.vm_exists(vm.get_name_hypervisor())):
            vm_manager.start_vm(vm.get_name_hypervisor())
        else:
            logs.logger.info(f"ERROR: vm '{vm.get_name_hypervisor()}' not found")
            return 0
    slice_part.set_status("active")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    return 1

def stop_slice_part_vms(slice_part):
    # Check status
    status = slice_part.get_status()
    #if(status != 'active'):
    #    logs.logger.info(f"Could not start slice_part because it is not active. Slice_part status = {status}")
    #    return 0
    slice_part.set_status("stopping")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    vms = slice_part.get_vms()
    # preparing vm shutdown
    logs.logger.info("Stopping slice_part vms")
    for vm in vms:
        if(vm_manager.vm_exists(vm.get_name_hypervisor())):
            vm_manager.shutdown_vm(vm.get_name_hypervisor())
        else:
            logs.logger.info(f"ERROR: vm '{vm.get_name_hypervisor()}' not found")
            return 0
    slice_part.set_status("ready")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    return 1

def deploy_slice_part(slice_part): 
    # Check status
    status = slice_part.get_status()
    if(status != 'accepted' and status != "deleted"):
        logs.logger.info(f"Could not deploy slice_part because it is not 'accepted'. Slice_part status = {status}")
        return 0
    # instantiate slice part
    elif(prepare_slice_part_vms(slice_part) == 0):
        # if error
        logs.logger.info("Stopping slice instantiation")
        # update slice_part status
        slice_part.set_status("failed")
        if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
        return 0
    # config by vim_type
    result = ""
    vim = VimDAO().select_vim(slice_part.get_vim_uuid())
    vim_type_name = vim.get_vim_type_name()
    logs.logger.info("VIM = " + vim_type_name)
    if(vim_type_name=="Kubernetes" or vim_type_name=="KUBERNETES"):
        result = kubernetes.config_vms(slice_part)
    elif(vim_type_name=="xen-vim"):
        result = xen.config_vms(slice_part)
    # update slice_part status
    slice_part.set_status("ready")
    if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
    logs.logger.info("Slice Part deployed successfully")
    return result

