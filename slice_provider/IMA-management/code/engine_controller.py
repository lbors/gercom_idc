from flask import Flask, url_for, request
from flask_request_params import bind_request_params
import yaml
import requests
import docker
import json
import socket
import time
import threading
from subprocess import call 

app = Flask(__name__)
adapter_dict = {}

# save and load file functions #################################################################

def save_dict():
    write_file = open("global_dict.json", 'w')
    json.dump(adapter_dict, write_file)
    write_file.close() 

@app.before_first_request
def load_dict():
    global adapter_dict
    try:
        file = open("global_dict.json", "r")
        data = file.read()
        file.close()
        adapter_dict = json.loads(data)
    except FileNotFoundError:
        print ('File "global_dict.json" does not exist. Resuming execution.')
    except AttributeError: 
        print ('File "global_dict.json" is not a valid Json file. Resuming execution with a empty dict.')  

@app.route('/')
def default_options():
    return 'Welcome to Resource and VM Management of IMA!'

@app.route('/listAdapters', methods = ['GET'])
def list_adapters():
    return str(json.dumps(adapter_dict, indent=2))

# container functions ########################################################################

def create_adapter(slice_id, slice_part_id, port, json_content):
    global adapter_dict

    agent_name = slice_part_id + '_adapter_ssh'
    ssh_ip = json_content['dc-slice-part']['VIM']['vim-ref']['ip-ssh']
    ssh_port = json_content['dc-slice-part']['VIM']['vim-ref']['port-ssh']
    ssh_user = json_content['dc-slice-part']['VIM']['vim-credential']['user-ssh']
    ssh_pass = json_content['dc-slice-part']['VIM']['vim-credential']['password-ssh']
    master_ip = "null"

    for j in range(len(json_content['dc-slice-part']['VIM']['vdus'])): 
        if str(json_content['dc-slice-part']['VIM']['vdus'][j]['vdu']['type']) == "master": 
            master_ip = str(json_content['dc-slice-part']['VIM']['vdus'][j]['vdu']['ip'])

    client = docker.from_env()
    client.containers.run("adapter_ssh:latest", detach=True, name=agent_name, ports={'1010/tcp': ('localhost', port)})

    master_data = ssh_ip + ":" + str(ssh_port) + ":" + ssh_user + ":" + ssh_pass + ":" + str(port) + ":" + master_ip

    while True:
        try:
            requests.post("http://0.0.0.0:" + str(port) + "/setSSH", data = master_data)
            break
        except requests.exceptions.ConnectionError:
            pass

    print("The Adapter", agent_name, "has started")

    if slice_id in adapter_dict:
            adapter_dict[slice_id].update({ 
                    slice_part_id: ({
                        "port":str(port),
                        "adapter_ssh_name": agent_name,
                        'ssh_ip': str(ssh_ip),
                        'ssh_port': str(ssh_port),
                        'ssh_user': str(ssh_user),
                        'ssh_pass': str(ssh_pass),
                        'master_ip': str(master_ip)
                    })
            })
    else:
        adapter_dict.update({
            slice_id: {
                slice_part_id: ({
                    'port': str(port),
                    'adapter_ssh_name': agent_name,
                    'ssh_ip': str(ssh_ip),
                    'ssh_port': str(ssh_port),
                    'ssh_user': str(ssh_user),
                    'ssh_pass': str(ssh_pass),
                    'master_ip': str(master_ip)
                    })
                }
            })

def start_slice_adapterv2(json_content):
    global adapter_dict
    threads = []
    slice_id = json_content['slice']['id']
    adapter_dict.update({slice_id:{}})

    for i in range(len(json_content['slice']['slice-parts'])):
        slice_part_id = json_content['slice']['slice-parts'][i]['dc-slice-part']['name']

        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind(('localhost', 0))
        port = s.getsockname()[1]
        s.close()

        t = threading.Thread(target=create_adapter,args=(slice_id, slice_part_id, port, json_content['slice']['slice-parts'][i],))
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    save_dict()
    return "The Adapters was started!"

def delete_container(container_name):
    client = docker.from_env()
    container = client.containers.get(container_name)
    try:
        container.stop()
    except:
        pass
    try:
        container.remove()
    except:
        pass


# management requests ########################################################################

@app.route('/necos/ima/start_management', methods = ['POST'])
def start_management():
    json_content = json.dumps(yaml.safe_load(request.data.decode('utf-8')))
    json_content = json.loads(json_content)
    start_slice_adapterv2(json_content)
    save_dict()

    return str(json.dumps(adapter_dict, indent=2))

@app.route('/necos/ima/stop_management', methods = ['POST'])
def stop_management():
    global adapter_dict
    post_data = request.data.decode('utf-8') # input example: "Telefonica"
    threads = []    

    for slice_part in adapter_dict[post_data]:
        if adapter_dict[post_data][slice_part]["adapter_ssh_name"] != "null":
            t = threading.Thread(target=delete_container,args=(adapter_dict[post_data][slice_part]["adapter_ssh_name"],))
            threads.append(t)
            t.start()
            
    for t in threads:
        t.join()

    adapter_dict.pop(post_data)
    save_dict()
    return str('The slice ' + post_data + ' has been deleted.')

@app.route('/necos/ima/update_management', methods = ['POST'])
def update_management():
    global adapter_dict
    data = yaml.safe_load(request.data.decode('utf-8'))
    json_content = json.dumps(data) 
    json_content = json.loads(json_content)

    if json_content['slice']['id'] in adapter_dict:
        i = 0
        for i in range(len(json_content['slice']['slice-parts'])):
            slice_part_id = json_content['slice']['slice-parts'][i]['dc-slice-part']['name']
            if slice_part_id in adapter_dict[json_content['slice']['id']]:
                print("Deleting adapter '" + slice_part_id + "_adapter_ssh" + "'")
                delete_container(slice_part_id+"_adapter_ssh")
                adapter_dict[json_content['slice']['id']].pop(slice_part_id)
            else:
                print("Creating adapter '" + slice_part_id + "_adapter_ssh" + "'")
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.bind(('localhost', 0))
                port = s.getsockname()[1]
                s.close()
                create_adapter(json_content['slice']['id'], slice_part_id, port, json_content['slice']['slice-parts'][i])
        save_dict()
        return str(json.dumps(adapter_dict, indent=2))
    else:
        return "Error: The slice ID in the yaml doesn't exists."

# service requests ########################################################################

@app.route('/necos/ima/deploy_service', methods = ['POST']) 
def create_service():
    data = yaml.safe_load(request.data.decode('utf-8'))
    json_content = json.dumps(data)
    json_content = json.loads(json_content)

    slice_id = json_content['slices']['sliced']['id']
    for slices_iterator in json_content['slices']['sliced']['slice-parts']:
        adapter_port = adapter_dict[slice_id][str(slices_iterator['name'])]['port']
        print("\t" + str(slices_iterator['name']) + ":")
        
        for service_it in slices_iterator['vdus']:
            print("Executing the commands: ")
            print(str(json.dumps(service_it['commands'], indent=2)))
            requests.post("http://0.0.0.0:" + str(adapter_port) + "/createService", data = json.dumps(service_it['commands']))

    if slice_id == 'IoTService_sliced':
        print("Waiting for commands to execute...")
        time.sleep(30)
            
    # return "Commands outputs = " + ('\n'.join(services_status))
    return 'The Service for ' + slice_id + ' was created!'

@app.route('/deleteService', methods = ['POST']) 
def delete_service():
    # loads the POST body e parse it to Json  
    data = yaml.safe_load(request.data.decode('utf-8'))
    json_content = json.dumps(data)
    json_content = json.loads(json_content)
    services_status = []

    for service_it in json_content['slice_parts']:
        # for each slice_part from yaml it adds N services, but only in one namespace
        adapter_port = adapter_dict[json_content['slice_id']][service_it['slice_part_id']]['port']
        resp = requests.post("http://0.0.0.0:" + str(adapter_port) + "/deleteService", data = json.dumps(service_it))
        parsed_resp = resp.content.decode('utf-8')
        services_status.append(parsed_resp)
    return ('\n'.join(services_status))

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port='5001')
