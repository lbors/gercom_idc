import time
from vm_factory.connection import uri, xen_user, xen_host, xen_port
from slice_creator import logs 
import libvirt, re
import sys, os
import xmltodict
from settings import dnsmasq_file
from xml.dom import minidom

def start_vm(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.create()
        logs.logger.info(f"Starting vm '{name}'")
    conn.close()

def shutdown_vm(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name) 
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.shutdown()
        logs.logger.info(f"Shutting Down vm '{name}'")
    conn.close()

def destroy_vm(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.destroy()
        logs.logger.info(f"Destroying vm '{name}'")
    conn.close()

def suspend_vm(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.suspend()
        logs.logger.info(f"Suspending vm '{name}'")
    conn.close()

def resume_vm(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.resume()
        logs.logger.info(f"Resuming vm '{name}'")
    conn.close()

def delete_vm(name):
    conn = libvirt.open(uri)
    logs.logger.info(f"Deleting vm '{name}'")
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        state = get_vm_state(name)
        if(state!="SHUTDOWN" and state!="SHUTOFF"): 
            vm.destroy()
        vm.undefine()
        #os.system(f"ssh {xen_user}@{xen_host} -p {xen_port} 'sudo lvremove --force /dev/vg0/{name}-disk'")
        os.system(f"virsh --connect={uri} vol-delete {name} --pool default")
        #os.system(f"virsh --connect={uri} pool-refresh vg0")
        os.system(f"virsh --connect={uri} pool-refresh default")
        logs.logger.info(f"Vm '{name}' deleted successfully")
    conn.close()
    return 1

def clone_vm(origin_vm, new_name):
    #command = f"virt-clone --connect={uri} -o {origin_vm} -n {new_name} -f /dev/vg0/{new_name}-disk --force"
    command = f"virt-clone --connect={uri} -o {origin_vm} -n {new_name} -f /var/lib/libvirt/images/{new_name} --force"
    conn = libvirt.open(uri)
    vm = conn.lookupByName(origin_vm)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {origin_vm}", file=sys.stderr)
        return 0
    conn.close()
    logs.logger.info(f"Clonning {origin_vm} to {new_name}...")
    os.system(command)
    logs.logger.info(f"{new_name} created successfully!")
    return 1


def set_vcpu(name, vcpus_amount):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.setVcpus(vcpus_amount)
        logs.logger.info(f"Vcpus updated successfully!")
    conn.close()

def set_memory(name, memory_amount):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        vm.setMemory(memory_amount)
        logs.logger.info(f"Memory updated successfully!")
    conn.close()

def list_active_vms():
    conn = libvirt.open(uri)
    vms = conn.listDomainsID()
    logs.logger.info("Active vms:")
    for vm_id in vms:
        vm = conn.lookupByID(vm_id)
        logs.logger.info(vm.name())
    conn.close()

def list_all_vms():
    conn = libvirt.open(uri)
    domainNames = conn.listDefinedDomains()
    if conn == None:
        logs.logger.info('Failed to get a list of domain names', file=sys.stderr)

    domainIDs = conn.listDomainsID()
    if domainIDs == None:
        logs.logger.info('Failed to get a list of domain IDs', file=sys.stderr)
    if len(domainIDs) != 0:
        for domainID in domainIDs:
            domain = conn.lookupByID(domainID)
            domainNames.append(domain.name)

    logs.logger.info("All (active and inactive domain names:")
    if len(domainNames) == 0:
        logs.logger.info('  None')
    else:
        for domainName in domainNames:
            logs.logger.info('  ' + str(domainName))

    conn.close()

def vm_exists(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        return 0
    else:
        return 1

def add_device(vm_name, xml):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(vm_name)
    # create new device by xml 
    vm.attachDeviceFlags(xml)

def remove_device(vm_name, xml):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(vm_name)
    # create new device by xml 
    vm.detachDeviceFlags(xml)

def get_domain(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        return vm

def get_xml(vm_name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(vm_name)
    xml_desc = vm.XMLDesc(0)
    # logs.logger.info(xml_desc)
    return xml_desc

def get_dhcp_ip(param):
    logs.logger.info(f"getting ip address for '{param}' ...")
    while True:
        with open(dnsmasq_file) as dnsmasq:
            for line in dnsmasq:
                line = line.split(" ")
                # logs.logger.info("'" + line[1] + "'")
                if(param==line[1] or param==line[3]): 
                    logs.logger.info("IP Address = " + line[2])
                    return line[2]
       
            #logs.logger.error(f"mac '{param}' not found")
            time.sleep(5)

def get_br_control_mac(vm_name):
    # get xml desc
    xml_desc = get_xml(vm_name)
    #logs.logger.info(xml_desc)
    doc = xmltodict.parse(xml_desc)
    mac = doc["domain"]["devices"]["interface"][1]["mac"]["@address"]
    logs.logger.info(mac)
    return mac

def get_vm_state(name):
    conn = libvirt.open(uri)
    vm = conn.lookupByName(name)
    if vm == None: 
        logs.logger.info(f"Failed to get the domain object {name}", file=sys.stderr)
        return 0
    else: 
        state, reason = vm.state()
        conn.close()
        logs.logger.info(f'VM {name}:')
        if state == libvirt.VIR_DOMAIN_NOSTATE:
            logs.logger.info('The state is VIR_DOMAIN_NOSTATE')
            return "NOSTATE"
        elif state == libvirt.VIR_DOMAIN_RUNNING:
            logs.logger.info('The state is VIR_DOMAIN_RUNNING')
            return "RUNNING"
        elif state == libvirt.VIR_DOMAIN_BLOCKED:
            logs.logger.info('The state is VIR_DOMAIN_BLOCKED')
            return "BLOCKED"
        elif state == libvirt.VIR_DOMAIN_PAUSED:
            logs.logger.info('The state is VIR_DOMAIN_PAUSED')
            return "PAUSED"
        elif state == libvirt.VIR_DOMAIN_SHUTDOWN:
            logs.logger.info('The state is VIR_DOMAIN_SHUTDOWN')
            return "SHUTDOWN"
        elif state == libvirt.VIR_DOMAIN_SHUTOFF:
            logs.logger.info('The state is VIR_DOMAIN_SHUTOFF')
            return "SHUTOFF"
        elif state == libvirt.VIR_DOMAIN_CRASHED:
            logs.logger.info('The state is VIR_DOMAIN_CRASHED')
            return "CRASHED"
        elif state == libvirt.VIR_DOMAIN_PMSUSPENDED:
            logs.logger.info('The state is VIR_DOMAIN_PMSUSPENDED')
            return "PMSUSPENDED"
        else:
            logs.logger.error('The state is unknown. The reason code is ' + str(reason))
            return('The state is unknown.')
        
