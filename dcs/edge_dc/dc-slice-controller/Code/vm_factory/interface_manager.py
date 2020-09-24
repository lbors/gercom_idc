from vm_factory.connection import uri
import libvirt
import sys, os

def list_interfaces():
    conn = libvirt.open(uri)
    if conn == None:
        print(f'Failed to open connection to {uri}', file=sys.stderr)
        return 0
    ifaceNames = conn.listInterfaces()
    for ifaceName in ifaceNames:
        print(ifaceName + " - Active")

    ifaceNames = conn.listDefinedInterfaces()
    for ifaceName in ifaceNames:
        print(ifaceName + " - Inactive")
    conn.close()

def create_interface(xml):
    conn = libvirt.open(uri)
    # create/modify a network interface
    iface = conn.interfaceDefineXML(xml, 0)
    # activate the interface
    iface.create(0)
    print("The interface name is: "+iface.name())
    conn.close()

