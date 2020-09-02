from tools.ssh import SSH
from settings import server_public_ip, server_public_port, xen_user, xen_port
import sys, os
from slice_creator import logs 

def generate_dinamic_port(base_port, slice_part_id, controller_id):
    dinamic_port = int(base_port) + int(str(slice_part_id))
    logs.logger.info(dinamic_port)
    return dinamic_port

            # base to create dinamic, slice obj,  control ip, port to forward
def config_forwarding_port(base_port, slice_part, vm_address, vm_port):
    if(base_port == "None"):
        return "None"
    logs.logger.info("Configuring iptables ...")
    # dinamic_port
    dinamic_port = generate_dinamic_port(base_port, slice_part.get_uuid(), slice_part.get_controller_id())
    # first command
    command = f"sudo iptables -t nat -A PREROUTING -p tcp --dport {dinamic_port} -j DNAT --to-destination {vm_address}:{vm_port}"
    logs.logger.info("Running '" + command + "'")
    os.system(f"ssh -o StrictHostKeyChecking=no {xen_user}@{server_public_ip} -p {server_public_port} '{command}'")
    # second command
    command = f"sudo iptables -t nat -A POSTROUTING -p tcp -d {vm_address} --dport {dinamic_port} -j SNAT --to-source {server_public_ip}"
    logs.logger.info("Running '" + command + "'")
    os.system(f"ssh -o StrictHostKeyChecking=no {xen_user}@{server_public_ip} -p {server_public_port} '{command}'")
    logs.logger.info("Iptables configuration done.")

    return {"ip": server_public_ip, "port": dinamic_port}


    
def delete_forwarding_port(base_port, slice_part, vm_port):
    logs.logger.info("Deleting iptables rules  ...")
    # dinamic_port
    dinamic_port = generate_dinamic_port(base_port, slice_part.get_uuid(), slice_part.get_controller_id())
    # get master control ip from iptables
    command = f"sudo iptables -t nat -L | grep '{dinamic_port} to:{server_public_ip}'" + " | awk '{print $5}'"
    vm_address = os.popen(f"ssh -o StrictHostKeyChecking=no {xen_user}@{server_public_ip} -p {xen_port} '{command}'").read().replace('\n', '')
    # first command
    command = f"sudo iptables -t nat -D PREROUTING -p tcp --dport {dinamic_port} -j DNAT --to-destination {vm_address}:{vm_port}"
    logs.logger.info("Running '" + command + "'")
    os.system(f"ssh -o StrictHostKeyChecking=no {xen_user}@{server_public_ip} -p {server_public_port} '{command}'")
    # second command
    command = f"sudo iptables -t nat -D POSTROUTING -p tcp -d {vm_address} --dport {dinamic_port} -j SNAT --to-source {server_public_ip}"
    logs.logger.info("Running '" + command + "'")
    os.system(f"ssh -o StrictHostKeyChecking=no {xen_user}@{server_public_ip} -p {server_public_port} '{command}'")
    logs.logger.info("Iptables configuration done.")

    return 1