from ryu.lib.ovs import vsctl
from settings import * 


def show_info():
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    command = vsctl.VSCtlCommand('show')
    ovs_vsctl.run_command([command])
    return command.result
   

def list_bridges():
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    command = vsctl.VSCtlCommand('list-br')
    ovs_vsctl.run_command([command])
    return command.result
   


def has_bridge(bridge_name):
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    command = vsctl.VSCtlCommand('list', ("Port", bridge_name))
    ovs_vsctl.run_command([command])
    
    return len(command.result) == 1 


def create_bridge(bridge_name):
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    command = vsctl.VSCtlCommand('add-br', (bridge_name, ))
    
    try:
        ovs_vsctl.run_command([command])
        return True
    except Exception as e:
        return False


def delete_bridge(bridge_name):
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    command = vsctl.VSCtlCommand('del-br', (bridge_name, ))
    
    try:
        ovs_vsctl.run_command([command])
        return True
    except Exception as e:
        return False


def set_vxlan(bridge_name, remote_address, vxlan_key):

    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    vxlan_name = "vxlan" + str(vxlan_key)

    command = vsctl.VSCtlCommand('add-port', (bridge_name, vxlan_name))

    try:
        ovs_vsctl.run_command([command])
    except Exception as e:
        return False
    
    local_address = LOCAL_IP

    #hack for working with 5tonic tunnel
    if remote_address in REMOTE_ADDRESS_MAPPING:
        remote_address = REMOTE_ADDRESS_MAPPING[remote_address]
    
    if remote_address == "10.1.0.3":
        local_address = "10.254.1.17"

    args = ['Interface', vxlan_name, 'type=vxlan',
                'options:remote_ip=%s' % remote_address, 'options:local_ip=%s' % local_address,
                'options:key=%s' % vxlan_key]

    command = vsctl.VSCtlCommand('set', args)

    try:
        ovs_vsctl.run_command([command])
        return True
    except Exception as e:
        return False

def remove_vxlan(bridge_name, vxlan_key):
    ovs_vsctl = vsctl.VSCtl(OVSDB_ADDR)

    vxlan_name = "vxlan" + str(vxlan_key)

    command = vsctl.VSCtlCommand('del-port', (bridge_name, vxlan_name))

    try:
        ovs_vsctl.run_command([command])
        return True
    except Exception as e:
        return False
    

if __name__ == "__main__":
    print (delete_bridge("br-vxlan"))
    print (create_bridge("br-vxlan"))
    #print (set_vxlan("br-vxlan", "6"))


    # print (delete_bridge("br-vxlan2"))
    # print (create_bridge("br-vxlan2"))
    # print (set_vxlan("br-vxlan2", "7"))

    #print (has_bridge("test"))
    #print (show_info())