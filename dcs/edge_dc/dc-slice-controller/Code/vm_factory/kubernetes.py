from tools.ssh import SSH
from settings import kube_credencials
from vm_factory import vm_manager
from dao.slice_part_dao import SlicePartDAO
from slice_creator import logs
from network_manager import network_manager, entrypoint_manager 
from settings import bridge_control
import random, time

def config_vms(slice_part):
    vms = slice_part.get_vms()
    join_cmd = None
    master_eth1 = None
    vms.sort()
    bridge = {}
    create_bridge_ts = time.time() # start
    bridge["vswitch"] = network_manager.create_resource(slice_part.get_uuid())
    create_bridge_te = time.time() # start
    logs.echo_to_file(f"Created OVS bridge in " + str(create_bridge_te - create_bridge_ts) + " seconds") #log
    for vm in vms:
        logs.logger.info("Configuring vm " + vm.get_name_hypervisor())
        logs.echo_to_file(f" ---- Configuring VM " + vm.get_name_hypervisor() + " ----") #log
        # add interfaces
        add_interfaces_ts = time.time() # start
        add_interfaces(vm.get_name_hypervisor(), bridge["vswitch"]["bridge-name"])
        mac_eth1 = vm_manager.get_br_control_mac(vm.get_name_hypervisor()) 
        add_interfaces_te = time.time() # start
        logs.echo_to_file(f"Added interfaces in " + str(add_interfaces_te - add_interfaces_ts) + " seconds") #log
        # start vm
        start_vm_ts = time.time()
        vm_manager.start_vm(vm.get_name_hypervisor())
        # connect to template 
        con = SSH(vm_manager.get_dhcp_ip(mac_eth1), kube_credencials["username"], kube_credencials["password"]) 
        start_vm_te = time.time()
        logs.echo_to_file(f"Started VM in " + str(start_vm_te - start_vm_ts) + " seconds") #log
        # change hostname
        config_vim_ts = time.time()
        logs.logger.info("Changing vm hostname to " + vm.get_name_hypervisor())
        con.exec_cmd(f"sed -i 's/MYHOSTNAME/{vm.get_name_hypervisor()}/g' /root/change_hostname.sh")
        con.exec_cmd("./change_hostname.sh")
        # change ip_address
        con.exec_cmd(f"sed -i 's/10.10.10.60/{vm.get_ip_address()}/g' /etc/netplan/01-netcfg.yaml")
        con.exec_cmd(f"sed -i 's/#//g' /etc/netplan/01-netcfg.yaml")        
        # apply network changes
        con.exec_cmd("netplan apply")
        # check vm_type
        if(vm.get_type() == "master"):
            master_eth1 = mac_eth1
            # setup master
            con.exec_cmd(f"sed -i 's/MYIP/{vm.get_ip_address()}/g' /root/install_pod.sh")
            con.exec_cmd("./install_pod.sh")
            logs.logger.info("Running proxy ...")
            con.exec_cmd("./proxy.sh > /dev/null 2>&1 &")
            logs.logger.info("Done.")
            join_cmd = con.exec_cmd("sudo kubeadm token create --print-join-command")
            logs.logger.info(join_cmd)
            if(len(join_cmd) < 20):
                logs.logger.info("ERROR: Join command invalid:")
                slice_part.set_status("failed")
                network_manager.remove_resource(slice_part.get_uuid()) 
                if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
                return 0
        elif (vm.get_type() == "worker"):
            if(join_cmd != None):
                # Setup worker
                logs.logger.info("Executing join command ...")
                logs.logger.info(join_cmd)
                con.exec_cmd(join_cmd + " >> kube-join.log")
                # deplay to join configure
                time.sleep(20)
            else:
                # Call function to get join_cmd
                pass
        # Remove configuration scripts
        con.exec_cmd("rm /root/change_hostname.sh")
        con.exec_cmd("rm /root/install_pod.sh")
        con.close()
        logs.logger.info(f"Configuration of vm {vm.get_name_hypervisor()} complete!")
        config_vim_te = time.time()
        logs.echo_to_file(f"Configured VIM in " + str(config_vim_te - config_vim_ts) + " seconds") #log
        logs.echo_to_file(f"Configured VM in " + str(config_vim_te - add_interfaces_ts) + " seconds") #log

    
    # configure monitoring tool
    config_monitoring_ts = time.time() # start
    config_monitoring_tool(slice_part, vm_manager.get_dhcp_ip(master_eth1))
    config_monitoring_te = time.time() #end
    logs.echo_to_file(f"Configured monitoring tool in " + str(config_monitoring_te - config_monitoring_ts) + " seconds") #log

    # generate slice_part entrypoints
    gen_entry_ts = time.time() # start
    entrypoints = generate_entrypoints(slice_part, vm_manager.get_dhcp_ip(master_eth1))
    gen_entry_te = time.time() # end
    logs.echo_to_file(f"Generated entrypoints in " + str(gen_entry_te - gen_entry_ts) + " seconds") #log
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
    # change this to create your own entrypoints following:
    forwarding = {
        # "VMPORT": ["BASEPORT", "ENTRYPOINT-JSONID"]
        "30900": ["19000", "monitoring-handle"], # prometheus
        "20000": ["20000", "monitoring-handle"], # netdata
        "8080": ["21000", "vim-handle"],  # kubernetes api
        "22": ["22000", "ssh-handle"]     # ssh
    }
    
    monitoring_tool = (slice_part.get_name().split("-"))[-1]
    if(monitoring_tool == 'netdata'): 
        forwarding.pop("30900", None)
    elif((monitoring_tool == 'prometheus')): 
        forwarding.pop("20000", None)
    # init entrypoints
    entrypoints = {}
    for key in forwarding:
        entrypoints[forwarding[key][1]] = entrypoint_manager.config_forwarding_port(forwarding[key][0], slice_part, master_control_ip, key)

    return entrypoints

def delete_entrypoints(slice_part):
    logs.logger.info("Removing  entrypoints")
    forwarding = {
        "30900": ["19000", "monitoring-handle"], # prometheus
        "20000": ["20000", "monitoring-handle"], # netdata
        "8080": ["21000", "vim-handle"],  # kubernetes api
        "22": ["22000", "ssh-handle"]     # ssh
    }

    monitoring_tool = (slice_part.get_name().split("-"))[-1]
    if(monitoring_tool == 'netdata'): 
        forwarding.pop("30900", None)
    elif((monitoring_tool == 'prometheus')): 
        forwarding.pop("20000", None)
    # init entrypoints
    entrypoints = {}
    for key in forwarding:
        entrypoint_manager.delete_forwarding_port(forwarding[key][0], slice_part, key)

    return 1

def config_monitoring_tool(slice_part, master_control_ip):
    logs.logger.info("Configuring monitoring tool")
    monitoring_tool = (slice_part.get_name().split("-"))[-1]
    con = SSH(master_control_ip, kube_credencials["username"], kube_credencials["password"])
    if(monitoring_tool == "netdata"): con.exec_cmd("./deploy_netdata.sh >> netdata.log")
    elif(monitoring_tool == "prometheus"): con.exec_cmd("./deploy_prometheus.sh >> prometheus.log")
    con.close()

def generate_vim_credentials():
    return {"user-ssh": kube_credencials["username"], "password-ssh": kube_credencials["password"]}

def add_vms(slice_part, vms, bridge, master_entrypoints, master_credentials):
    logs.logger.info("Adding new vms...")
    join_cmd = None
    master_eth1 = None
    vms.sort()
    for vm in vms:
        logs.logger.info("Configuring vm " + vm.get_name_hypervisor())
        # add interfaces
        add_interfaces_ts = time.time() # start
        add_interfaces(vm.get_name_hypervisor(), bridge["bridge-name"])
        mac_eth1 = vm_manager.get_br_control_mac(vm.get_name_hypervisor()) 
        add_interfaces_te = time.time() # start
        logs.echo_to_file(f"Added interfaces in " + str(add_interfaces_te - add_interfaces_ts) + " seconds") #log
        # start vm
        start_vm_ts = time.time()
        vm_manager.start_vm(vm.get_name_hypervisor())
        # connect to template 
        con = SSH(vm_manager.get_dhcp_ip(mac_eth1), kube_credencials["username"], kube_credencials["password"]) 
        start_vm_te = time.time()
        logs.echo_to_file(f"Started VM in " + str(start_vm_te - start_vm_ts) + " seconds") #log
        # change hostname
        config_vim_ts = time.time()
        logs.logger.info("Changing vm hostname to " + vm.get_name_hypervisor())
        con.exec_cmd(f"sed -i 's/MYHOSTNAME/{vm.get_name_hypervisor()}/g' /root/change_hostname.sh")
        con.exec_cmd("./change_hostname.sh")
        # change ip_address
        con.exec_cmd(f"sed -i 's/10.10.10.60/{vm.get_ip_address()}/g' /etc/netplan/01-netcfg.yaml")
        con.exec_cmd(f"sed -i 's/#//g' /etc/netplan/01-netcfg.yaml")        
        # apply network changes
        con.exec_cmd("netplan apply")
        # check vm_type
        if(vm.get_type() == "master"):
            master_eth1 = mac_eth1
            # setup master
            con.exec_cmd(f"sed -i 's/MYIP/{vm.get_ip_address()}/g' /root/install_pod.sh")
            con.exec_cmd("./install_pod.sh")
            logs.logger.info("Running proxy ...")
            con.exec_cmd("./proxy.sh > /dev/null 2>&1 &")
            logs.logger.info("Done.")
            join_cmd = con.exec_cmd("sudo kubeadm token create --print-join-command")
            logs.logger.info(join_cmd)
            if(len(join_cmd) < 20):
                logs.logger.info("ERROR: Join command invalid:")
                slice_part.set_status("failed")
                network_manager.remove_resource(slice_part.get_uuid()) 
                if(SlicePartDAO().update_slice_part(slice_part) != 1): return 0
                return 0
        elif (vm.get_type() == "worker"):
            if(join_cmd != None):
                # Setup worker
                logs.logger.info("Executing join command ...")
                logs.logger.info(join_cmd)
                con.exec_cmd(join_cmd + " >> kube-join.log")
                # deplay to join configure
                time.sleep(20)
            else:
                master_entrypoints['ip-ssh'] = get_master_control_ip(slice_part)
                master_entrypoints['port-ssh'] = 22
                join_cmd = get_join_cmd(master_entrypoints, master_credentials)
                logs.logger.info("Executing join command ...")
                logs.logger.info(join_cmd)
                con.exec_cmd(join_cmd + " >> kube-join.log")
                # deplay to join configure
                time.sleep(20)
        # Remove configuration scripts
        con.exec_cmd("rm /root/change_hostname.sh")
        con.exec_cmd("rm /root/install_pod.sh")
        con.close()
        logs.logger.info(f"Configuration of vm {vm.get_name_hypervisor()} complete!")
        config_vim_te = time.time()
        logs.echo_to_file(f"Configured VIM in " + str(config_vim_te - config_vim_ts) + " seconds") #log
        logs.echo_to_file(f"Configured VM in " + str(config_vim_te - add_interfaces_ts) + " seconds") #log

def get_join_cmd(master_entrypoints, master_credentials):
    con = SSH(master_entrypoints["ip-ssh"], master_credentials["user-ssh"], master_credentials["password-ssh"], master_entrypoints["port-ssh"]) 
    join_cmd = con.exec_cmd("sudo kubeadm token create --print-join-command")
    logs.logger.info(join_cmd)
    if(len(join_cmd) < 20):
        logs.logger.info("ERROR: Join command invalid:")
        return 0
    else:
        return join_cmd

def get_master_control_ip(slice_part):
    vms = slice_part.get_vms()
    vms.sort()
    for vm in vms:
        if(vm.get_type() == "master"):
            return vm_manager.get_dhcp_ip(vm.get_name_hypervisor())