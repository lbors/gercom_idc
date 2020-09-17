from model.slice_part import SlicePart
from dao.slice_part_dao import SlicePartDAO
from dao.vim_dao import VimDAO
from vm_factory import vm_manager
from vm_factory import kubernetes, xen
from slice_creator import logs
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

def clone_new_vms(vms):
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

def update_slice_part(slice_part, vms, bridge, master_entrypoints, master_credentials): 
    # instantiate new vms
    result = ""
    vim = VimDAO().select_vim(slice_part.get_vim_uuid())
    vim_type_name = vim.get_vim_type_name()
    logs.logger.info("VIM = " + vim_type_name)
    # clone vms
    if(not clone_new_vms(vms)):
        return 0
    # check vim_type
    if(vim_type_name=="Kubernetes" or vim_type_name=="KUBERNETES"):
        result = kubernetes.add_vms(slice_part, vms, bridge, master_entrypoints, master_credentials)
    return result

