
# DC Slice Controller


## SSH keys generation (at the host running the dcsc service)

We need to generate access keys and copy them so the dc-slice-controller can connect to Xen server:

----------
	$ ssh-keygen
	$ ssh {USER}@{MY_IP} mkdir -p .ssh
	$ cat .ssh/id_rsa.pub | ssh {USER}@{XEN_SERVER_IP} 'cat >> .ssh/authorized_keys'


## XEN host libvirt user configuration (at the host where the vms will be instantiated):
	Change {USER} for the new username
	$ sudo adduser {USER}
	$ sudo usermod -aG sudo {USER}
	$ sudo usermod -G libvirt -a {USER}
	$ sudo visudo
	
	Add the follow line:
	$ {USER} ALL=(ALL) NOPASSWD:ALL

## Xen host network configuration (at the host where the vms will be instantiated):

    $ sudo apt-get install bridge-utils

    create the control bridge 
    $ sudo brctl addbr br-control

    we use the network 10.16.10.0/24 as our control network.

    $ sudo ifconfig br-control 10.16.10.100

    $ sudo apt-get install openvswitch-switch

    Set TCP listen address
    $ ovs-vsctl set-manager "ptcp:6640"

    if you have any firewall configured, you need to make sure to included the apropriated entries:

    # Allow trafic for vxlan
    iptables -A INPUT -p udp --dport 4789 -j ACCEPT

    #allow ovs agent connection on control network only
    iptables -A INPUT -p tcp  -s 10.16.0.0/16 --dport 6640 -j ACCEPT
    
## Running using our provided Docker containers (requires Docker and docker-compose)

### Copy your ssh keys to the keys directory:

	$ cp .ssh/* keys/

### Edit the settings file according to your needs:

	Change the attributes of settings file to suit your environment.
	Settings file:
    > Code/settings.py
	
### To execute DC Slice Controller run:

	$ docker-compose up

In case you receive a mysql server connection error on your first run, stop the containers and rerun again.

If everything goest right, you will be able to test the running webserver at:

	$ curl MY_IP:5000/

You should receive an output like:

	$ {
  		"result": "DCSC webserver is up and running"
	  }

### Configure docker SSH
Acess docker image "dcslicecontroller_dcsc-webserver"
```shell
docker exec -it DOCKER_ID /bin/bash
```
Install nano:
```shell
apt-get install nano
```
Edit '/etc/ssh/ssh_config'
```shell
nano /etc/ssh/ssh_config
```
Add the following line to file:
```shell
StrictHostKeyChecking no
```

## Manual Installation:

* Based on Ubuntu Server 18.04

### Dependencies
* Python3
* MySQLdb
* Libvirt
* Xen
* Lvm

### DC SC Requeriments

#### Python3.6
----------
	$ sudo curl https://bootstrap.pypa.io/get-pip.py | sudo -H python3.6
	$ sudo pip install pip==9.0.3


#### MySQL
----------
	$ sudo apt-get install python3.6-dev libmysqlclient-dev
	$ sudo pip3 install mysqlclient
	$ sudo apt install mysql-server
	$ mysql_secure_installation
    * Create a database named as 'dc_slice_controller' and import database schema with the following command:
	$ mysql -h MY_IP -u YOUR_USER -p dc_slice_controller < Database/install_db.sql


#### Flask
----------
	$ sudo pip3 install flask-restful


#### Paramiko
----------
	$ pip3 install paramiko
	$ pip3 install cryptography==2.4.2


#### YAML
----------
	$ pip3 install ruamel.yaml
	$ pip3 install pyyaml


#### Libvirt
----------
	$ sudo apt-get install libvirt-dev
	$ sudo apt install virtinst
	$ sudo pip3 install libvirt-python


#### Logger
----------
	$ sudo pip3 install logger
	$ sudo pip3 install jsonify
	$ sudo pip3 install wraps
	$ sudo pip3 install request


#### OVS
----------
	$ sudo pip3 install ovs==2.10.0
	$ sudo pip3 install ryu


#### Network Manager dependencies
----------
    $ sudo pip3 install -r Code/network_manager/requirements.txt

#### Execute DC Slice Controller:
----------
	* Configure the settings.py file on /Code/settings
	* Execute the run.py file on /Code/run.py


## Consuming the APIs:
To use the DC Slice Controller API you need to provide the corresponding yaml files. They can be found in *Yaml* folder. MY_IP is the ip where the webserver is running.

### Populate database
To create and control slices, you need to populate the database first. Use the following api calls:

-------------
    # curl -X POST --data-binary @Yaml/POST/post_user.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/user
	# curl -X POST --data-binary @Yaml/POST/post_controller.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/controller
    # curl -X POST --data-binary @Yaml/POST/post_vim_type.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/vim_type
    # curl -X POST --data-binary @Yaml/POST/post_template.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/template
	# curl -X POST --data-binary @Yaml/POST/post_host.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/host

### Request slice_part:
To request slice_part you need to use the yaml file *Yaml/POST/request_slice_part*. You must modify the fields to register your slice_part as desired.

-------------
    # curl -X POST --data-binary @Yaml/POST/request_slice_part.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/slice_part/request

### Activate slice_part:
To activate slice_part you need to use the yaml file *Yaml/POST/activate_slice_part*. You must enter the uuid to activate its slice_part.

-------------
    # curl -X POST --data-binary @Yaml/POST/activate_slice_part.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/slice_part/activate

### Stop slice_part:
To stop slice_part you need to use the yaml file *Yaml/POST/stop_slice_part*. You must enter the uuid to stop its slice_part.

-------------
    # curl -X POST --data-binary @Yaml/POST/stop_slice_part.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/slice_part/stop

### Delete slice_part:
To delete slice_part you need to use the template *Yaml/DELETE/delete_slice_part*. You must enter the uuid to delete its slice_part.

-------------	
    # curl -X DELETE --data-binary @Yaml/DELETE/delete_slice_part.yaml -H "Content-type: text/x-yaml" http://MY_IP:5000/slices_part

### Check slice_part service:
To check the Kubernetes service you must access the master node and use kubectl to list Kubernetes nodes. In your XEN host execute the following command:
```shell
virsh console VM-MASTER
```
Go in with your credencials and execute the following command:
```shell
kubectl get nodes
```
