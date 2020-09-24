from tools.ssh import SSH
from settings import osm_credencials
from vm_factory import vm_manager
from dao.slice_part_dao import SlicePartDAO
from slice_creator import logs
from network_manager import network_manager, entrypoint_manager 
from settings import bridge_control
import random, time

def config_vms(slice_part):
    vms = slice_part.get_vms()
    master_eth1 = None 
    vms.sort()
    bridge = {}
    bridge["vswitch"] = network_manager.create_resource(slice_part.get_uuid())
    for vm in vms:
        logs.logger.info("Configuring vm " + vm.get_name_hypervisor())
        # add interfaces
        add_interfaces(vm.get_name_hypervisor(), bridge["vswitch"]["bridge-name"])
        mac_eth1 = vm_manager.get_br_control_mac(vm.get_name_hypervisor()) 
        # start vm
        vm_manager.start_vm(vm.get_name_hypervisor())
        # connect to template 
        con = SSH(vm_manager.get_dhcp_ip(mac_eth1), osm_credencials["username"], osm_credencials["password"]) 
        # change hostname
        logs.logger.info("Changing vm hostname to " + vm.get_name_hypervisor())
        con.exec_cmd(f"sed -i 's/MYHOSTNAME/{vm.get_name_hypervisor()}/g' /root/change_hostname.sh")
        con.exec_cmd("./change_hostname.sh")
        # change ip_address
        con.exec_cmd(f"sed -i 's/10.10.10.60/{vm.get_ip_address()}/g' /etc/netplan/01-netcfg.yaml")
        con.exec_cmd(f"sed -i 's/#//g' /etc/netplan/01-netcfg.yaml")
        # apply network changes
        con.exec_cmd("netplan apply")
        master_eth1 = mac_eth1
        # Remove configuration scripts
        con.exec_cmd("rm /root/change_hostname.sh")
        con.close()
        logs.logger.info(f"Configuration of vm {vm.get_name_hypervisor()} complete!")
    
    # generate slice_part entrypoints
    entrypoints = generate_entrypoints(slice_part, vm_manager.get_dhcp_ip(master_eth1))
    logs.logger.info(f"Configuration of slice_part {slice_part.get_name()} complete!")

    # get vim credentials
    credentials = {}
    credentials["vim-credential"] = generate_vim_credentials()

    # return informations 
    info = {}
    info.update(entrypoints)
    info.update(bridge)
    info.update(credentials)
    return info

def add_interfaces(vm_name, bridge_eth0):
    #create interface of slice
    xml_eth0 = f"""
        <interface type='bridge'>
        <source bridge='{bridge_eth0}'/>
        <virtualport type='openvswitch'/>
        <script path='vif-openvswitch'/>
        </interface>
    """
    vm_manager.add_device(vm_name, xml_eth0)
    logs.logger.info("Interface eth0 added")

    # create interface of control
    xml_eth1 = f"""
        <interface type='bridge'>
        <source bridge='{bridge_control}'/>
        </interface>
    """
    vm_manager.add_device(vm_name, xml_eth1)
    logs.logger.info("Interface eth1 added")

def remove_eth1(vm_name, mac): 
    xml_eth1 = f"""
        <interface type='bridge'>
        <mac address='{mac}'/>
        <source bridge='{bridge_control}'/>
        </interface>
    """
    vm_manager.remove_device(vm_name, xml_eth1)
    return 

def generate_entrypoints(slice_part, master_control_ip):
    logs.logger.info("Generanting  entrypoints")
    forwarding = {
        "80": ["21000", "vim-handle"],  	# osm api
        "22": ["22000", "ssh-handle"]     	# ssh
    }
    
    # init entrypoints
    entrypoints = {}
    for key in forwarding:
        entrypoints[forwarding[key][1]] = entrypoint_manager.config_forwarding_port(forwarding[key][0], slice_part, master_control_ip, key)

    return entrypoints

def delete_entrypoints(slice_part):
    logs.logger.info("Generanting  entrypoints")
    forwarding = {
        "80": ["21000", "vim-handle"],  	# osm api
        "22": ["22000", "ssh-handle"]     	# ssh
    }
    
    # init entrypoints
    for key in forwarding:
        entrypoint_manager.delete_forwarding_port(forwarding[key][0], slice_part, key)
    return 1

def generate_vim_credentials():
    return {"user-ssh": osm_credencials["username"], "password-ssh": osm_credencials["password"]}

