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





if __name__ == "__main__":
    print (delete_bridge("br-vxlan"))
    print (create_bridge("br-vxlan"))


    # print (delete_bridge("br-vxlan2"))
    # print (create_bridge("br-vxlan2"))
    # print (set_vxlan("br-vxlan2", "7"))

    #print (has_bridge("test"))
    #print (show_info())