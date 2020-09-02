from flask import Flask, url_for, request
from flask_request_params import bind_request_params
import yaml
import requests
import docker
import json
import paramiko
import time

app = Flask(__name__)
ssh_port = 22
ssh_ip = '200.133.239.3X'
ssh_user = 'necos'
ssh_pass = 'necos'
master_ip = '200.18.102.6X'

@app.route('/setSSH', methods = ['POST'])
def set_SSH():
    global ssh_port, ssh_ip, ssh_user, ssh_pass, master_ip
    post_data = request.data.decode('utf-8')
    post_data = post_data.split(':')
    ssh_ip = post_data[0]
    ssh_port = post_data[1]
    ssh_user = post_data[2]
    ssh_pass = post_data[3]
    master_ip = post_data[5]

    return 'OK'

@app.route('/')
def default_options():
    return 'Welcome to the SSH adapter of Resource and VM Management'

@app.route('/createService', methods = ['POST'])
def create_service():
    global ssh_port, ssh_ip, ssh_user, ssh_pass, master_ip
    yaml_content = request.data.decode('utf-8')

    data = yaml.safe_load(yaml_content)
    json_content = json.dumps(data)
    json_content = json.loads(json_content)
    print(json_content)

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=ssh_ip,port=ssh_port,username=ssh_user,password=ssh_pass)

    for commands in json_content: 
        print(commands)

        if "sed" in commands:
            commands = commands.replace("$coreip", master_ip)
        
        stdin, stdout, stderr = ssh.exec_command(commands)  # Non-blocking call
        exit_status = stdout.channel.recv_exit_status()          # Blocking call
        if exit_status == 0:
            print ("Command Ok!")
        else:
            print("Error", exit_status) 
    ssh.close()

    return "OK"

@app.route('/getService', methods = ['POST'])
def get_service():
    return "OK"
    


@app.route('/deleteService', methods = ['POST']) 
def delete_service():
    return "OK"
    
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port='1010')
