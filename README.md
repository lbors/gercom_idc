# MUSTS (MUlti-Slice/Tenant/Service)

This file provides guidance to install and run the MUSTS demo.
The MUSTS demo implements a subset of the functionalities related to slice creation, slice decommission, slice monitoring, service deployment and elasticity.

Copyright 2019 NECOS Consortium
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file and software referenced except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

A video of the Demo execution can be found [here](https://www.youtube.com/watch?v=8GDtCkgLjtw).

## Introduction
The main objective of the MUSTS demo is to demonstrate slice creation, slice decommission, slice monitoring, service deployment, and elasticity upgrade (both vertical and horizontal) using the NECOS platform. In this demo, two slices are created by two different tenants, one running a Touristic service and the other running an Internet of Things (IoT) service.

The touristic Content Delivery Network (CDN) service has the following characteristics:
* The service delivers touristic content to users based on their geographic location. 
* The service involves a core cloud Web server hosting all content (videos/web pages), and edge cloud nodes hosting a single video and a web site related to their geographic location. As an example, consider that Spain hosts an edge cloud node. Content requests from a visitor sightseeing Spain are directed either to the local edge cloud server, in case that the requests are related to Spain, or to the core server if his requests are irrelevant to his position.
* The end-to-end slice to be created in the touristic CDN service is composed by four slice-parts: the dc-core (in this demo located at UNICAMP) and three dc-edges (in this demo represented by SPAIN, GREECE, and UFSCAR). 
* Each slice part is composed of a single VM. 
* The dc-core slice-part accommodates the DNS load balancer, an Apache web server which provides all the web pages, VLC video streaming servers, and the Grafana and Influx-db monitoring tools in the touristic Web Server. 
* The dc-edges host the content services (flask and VLC) and benchmarking tools (jmeter). These services are containerized using Docker.

The IoT service has the following characteristics:
* It emulates an IoT scenario where monitoring devices with wireless communication and multiple sensors (temperature, humidity, light, and gps) periodically transmits precise monitored data to an edge node, which in turn, transforms the data to be transmitted to the cloud system. 
* The service is built using [Dojot](http://www.dojot.com.br/) and a load testing tool for Message Queuing Telemetry Transport (MQTT) IoT devices (for example, mosquitto\_pub tool, available with mosquitto_clients package).
* Beyond demonstrating the slice creation, slice decommission, slice monitoring and service deployment, this slice showcases the service update and elasticity upgrade, both vertical and horizontal.
* the end-to-end slice to be created in the IoT service is composed by two slice-parts: the dc-core (in this demo located at UNICAMP) and the dc-edge (in this demo located at 5Tonic).
* The dc-core (slice-part 1) runs the cloud Dojot micro services, which are responsible for: managing the IoT devices life cycle, storing telemetry data, and providing REST and socket.io interfaces to retrieve data about the devices. 
* The dc-edge (slice-part 2) runs the edge Dojot micro service, called MQTT IoT-Agent.
* The slice-part 1 is a set of three VMs managed by the Kubernetes VIM (one VM hosting the master node and the other two representing the worker nodes). 
* The slice-part 2 is a set of two VMs managed by the Kubernetes VIM (one VM hosting the master node and the other representing the worker node). The edge Dojot micro service is deployed in the worker node.

Additional details can be found in [NECOS](http://www.h2020-necos.eu/) Project [deliverables](http://www.h2020-necos.eu/documents/deliverables/) (D4.2, D5.2 and D6.2).


## Components of MUSTS
This demo uses the following NECOS components: 
- **Tenant Domain**: Slice Activator, Service Orchestrator. 
- **Slice Provider Domain**: Slice Spec Processor, Slice Builder, Slice Resource Orchestrator (SRO), Slice Database, IMA, Service Orchestrator Adaptor.
- **Marketplace Domain**: Slice Broker.
- **Resource Provider Domain**: DC Slice Controller, WAN Slice Controller agent, Marketplace Slice Agent.
 

## Structure of the repository 
Each domain contains a respective folder with all the source code and files needed to run the components of the given domain.

- `/Marketplace`: Directory containing all the necessary files for running the Marketplace components.
- `/Resource Provider` : Directory containing all the necessary files for running the Resource Provider components.
- `/Slice Provider` : Directory containing all the necessary files for running the Slice Provider components and the Teant components.


The `Tenant` folder contains the yaml files and/or scripts required for creating the slices and deploying the services.


- `/Tenant/Touristic/slice`: Directory containing the yaml required to create the touristic CDN slice.
- `/Tenant/Touristic/service`: Directory containing the files required to deploy the touristic CDN service.
- `/Tenant/Dojot/slice`: Directory containing the yaml required to create the IoT slice  
- `/Tenant/Dojot/service`: Directory containing the yamls required to deploy and exercise elasticity in the IoT service. 


## How to install MUSTS
The MUSTS demo can be executed in one or more machine. In our demonstration, we use six servers: one for the Marketplace, one to host the Tenant and Slice Provider, and four machines for the Resource Providers.
In order to run the MUSTS demo, all the serves need to have [docker] and [docker-compose] installed. You will also need

- Python 3.7 
- [libvirt], [Xen] and Lvm in the machines that play the role of Resource Provider
- [InfluxDB] in the machine that plays the role of Slice Provider

Then, download the MUSTS source code from gitlab and install the software components as will be described below.
```bash
git clone https://gitlab.com/necos/demos/musts.git
```

### Marketplace
The Marketplace domain must run the Slice Broker Component. In this demo, we consider a single Marketplace domain.

#### Slice Broker
Enter the `/Marketplace` directory and run

```bash
docker-compose run
```

The command above will build the containers for the Slice Broker component. The component will be running at port 8000. 

### Resource Provider
The DC Slice Controller, WAN Slice Controller and the Slice Agent components need to be installed in each domain playing the role of Resource Provider. The WAN Slice Controller is implemented by two components: the WAN Master and the WAN Agent. The WAN Master have to be installed in one of the domain playing the role of Resource Provider while the WAN Agent must be installed in every domain playing the role of Resource Provider. 

#### DC Slice Controller

Enter the `/Resource Provider/dc-slice-controller` directory and run

```bash
docker-compose run
```

The command above will build the containers for the DC Slice Controller component. The component will be running at port 5000.


#### Wan Slice Controller Master

Enter the `/Resource Provider/wan-slice-controller-master` directory and run

```bash
docker-compose run
```

The command above will build the containers for the WAN Slice Master component. The component will be running at port 8080.

#### Wan Slice Controller Agent

Enter the `/Resource Provider/wan-slice-controller-agent` directory and run

```bash
docker-compose run
```

The command above will build the containers for the WAN Slice Agent component. The component will be running at port 3030.

#### Slice Agent

Enter the `/Resource Provider/marketplace-slice-agent` directory and run

```bash
docker-compose run
```

The command above will build the containers for the Slice Agent component. The component will be running at port 8001.


### Slice Provider
The components below need to be installed in the domain playing the role of Slice Provider. In the MUSTS demo, we consider a single domain acting as Slice Provider.

#### IMA Management
Enter the `/Slice Provider/IMA-management` directory and install Python dependencies using the requirements.txt file located at the `Slice Provider/IMA-management` folder. Then run the command

```bash
python3.7 -m pip install -r IMA-management/requirements.txt
```

Run Docker Images for the Adapters Components

```bash
cd IMA-management
sudo docker build -f Dockerfile -t adapter_ssh:latest .
```

Run the IMA Management Application 

```bash
sudo python3.7 IMA-management/code/engine_controller.py
```

Open a new terminal to check if the application starts

```bash
curl localhost:5001/
```

The following message will show:
```bash
Welcome to Resource and VM Management of IMA!
```    

#### IMA Monitoring
Go to the `/Slice Provider/IMA-monitoring/Lattice` folder and compile the IMA-Lattice component using the instructions provided in the folder. After compiling the code, run the IMA-Lattice application opening a new terminal and executing

```bash
cd ~/lattice/jars/
java -cp monitoring-bin-controller-2.0.1.jar mon.lattice.control.controller.json.ZMQController controller.properties
```

Go to the `/Slice Provider/IMA-monitoring/Controller` folder and run the IMA-Controller application opening a new terminal and executing

```bash
java -jar monitoring-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Then, install InfluxDB following the instructions in the InfluxDB website and create the database:

```bash
$ influx
> CREATE DATABASE E2E_SLICE
```    

#### Service Orchestrator Adaptor
The Service Orchestrator Adaptor interacts with the SRO. Edit the settings.py file, located at `/Slice Provider/service-orchestrator-adaptor` and configure the ip address where the SRO is running. Then, enter the `/Slice Provider/service-orchestrator-adaptor` directory and run 

```bash
docker-compose run
```

The command above will build the containers for the Service Orchestrator component. The component will be running at port 5011.

#### Slice Builder
The Slice Builder component interacts with the Slice Broker and the SRO. Edit the settings.py file, located at `/Slice Provider/slice-builder` and configure the ip addresses where the Slice Broker and the SRO components are running. Then, enter the `/Slice Provider/slice-builder` directory and run 

```bash
docker-compose run
```

The command above will build the containers for the Slice Builder component. The component will be running at port 5003. 

#### Slice Spec Processor
The Slice Spec Processor interacts with the Slice Builder. Edit the settings.py file, located at `/Slice Provider/slice-spec-processor`, and configure the ip address where the Slice Builder is running. Then, enter the `/Slice Provider/slice-spec-processor` directory and run


```bash
docker-compose run
```

The command above will build the containers for the Slice Spec Processor component. The component will be running at port 5002.

#### SRO
The SRO interacts with the IMA and the Slice Builder. Edit the config.py file, located at `/Slice Provider/SRO` and configure the ip addresses where the IMA and the Slice Builder are running. Then, enter the `/Slice Provider/SRO` directory and run 

```bash
docker-compose run
```

The command above will build the containers for the SRO component. The component will be running at port 5005.

### Tenant
The Service Orchestrator and Slice Activator components run in the Tenant domain. The source code of these components is availabe at the `/Slice Provider` folder.

#### Slice Activator
The Slice Activator component interacts with the Slice Spec Processor and the SRO. Edit the settings.py file, located at `/Slice Provider/slice-activator`, and configure the ip addresses where the Slice Spec Processor and the SRO are running. Then, enter the `/Slice Provider/slice-activator` directory and run 

```bash
docker-compose run
```

The command above will build the containers for the Slice Activator component. The component will be running at port 5001.

#### Service Orchestrator
The Service Orchestrator interacts with the Service Orchestrator Adaptor (SOA). Edit the settings.py file, located at `/Slice Provider/service-orchestrator` and configure the ip address where the SOA is running. Then, enter the `/Slice Provider/service-orchestrator` directory and run 

```bash
docker-compose run
```

The command above will build the containers for the Service Orchestrator component. The component will be running at port 5010.


[docker]: <https://docs.docker.com/install/>
[docker-compose]: <https://docs.docker.com/compose/install/>
[libvirt]:<https://libvirt.org/>
[Xen]:<https://xenproject.org/>
[InfluxDB]:<https://www.influxdata.com/>

## How to run MUSTS

### Running the IoT service
**Step 1**: To create a slice as described in the dojot_slice_iot_slice-activator-to-slice-spec-processor.yaml file, located at `/Tenant/Dojot/slice`, enter the following command
```bash
curl -X POST --data-binary @dojot_slice_iot_slice-activator-to-slice-spec-processor.yaml -H "Content-type: text/x-yaml" http://<slice-activator-ip>:5001>/slice_activator/create_slice
```
As a response to this call, the Slice Activator will receive a yaml file describing the slice created.

**Step 2**: To deploy the Dojot service, edit the iot_service-orchestrator-to-service-orchestrator-adaptor.yaml file, located at `/Tenant/Dojot/service`, and replace the string "master-core-ip" (see the example below) by the ip address of the master node running in the dc-core. This ip address is provided by the yaml file returned to the Slice Activator. Also, configure the fields names inside the vdu section (see the example below) with the names of the master nodes in the yaml returned to the Slice Activator. 
```bash
name: dc-slice1
      ...
      - vdu:
        name: vm-master-name-slice1
        ...
        
              commands: 
                ...
                - sed -i "s/REPLACE/master-core-ip/"

name: dc-slice2
      ...
      - vdu:
        name: vm-master-name-slice2
        ...
              commands: 
              ...
              - sed -i "s/REPLACE/master-core-ip/"
```

Then, enter the following command

```bash
curl -X POST --data-binary iot_service-orchestrator-to-service-orchestrator-adaptor.yaml -H "Content-type: text/x-yaml" http://<service-orchestrator-ip>:5010/service_orchestrator/deploy_service
```
The Dojot Web GUI will be available at `http://<dc-slice-controller-at-core-ip>:30001`

**Step 3**: To exercise the vertical elasticity, edit the  vertical-elasticity.yaml file, located at `/Tenant/Dojot/service`, and replace the string "master-core-ip" (see the example in Step 2) by the ip address of the master node running in the dc-core. This ip address is provided by the yaml file returned to the Slice Activator. Also, configure the fields names inside the vdu section (see the example in Step 2) with the names of the master nodes in the yaml returned to the Slice Activator. 
Then, enter the following command
```bash
curl -X POST --data-binary  vertical-elasticity.yaml -H "Content-type: text/x-yaml" http://<service-orchestrator-ip>:5010/service_orchestrator/deploy_service
```
You will need a load testing tool for MQTT to stress the IoT Agent microservice. The vertical elasticity will be triggered when CPU usage is higher than 80%. This threshold can be changed in the yaml file. In this demo, we use the [Locust](https://locust.io/) tool to stress the IoT Agent. To setup the Locust follow the steps below
- Set up a virtual machine with git, docker and docker-compose, having 4 cpus and 8GB of RAM;
- Download the Locust script;
- Set the environment variables of the script;
- Run the Locust Docker-Compose with 4-Slaves

**Step 4**: To exercise the horizontal elasticity, edit the horizontal-elasticity.yaml file, located at `/Tenant/Dojot/service`, and replace the string "master-core-ip" (see the example in Step 2) by the ip address of the master node running in the dc-core. This ip address is provided by the yaml file returned to the Slice Activator. Also, configure the fields names inside the vdu section (see the example in Step 2) with the names of the master nodes in the yaml returned to the Slice Activator. 
Then, enter the following command
```bash
curl -X POST --data-binary  horizontal-elasticity.yaml  -H "Content-type: text/x-yaml" http://<service-orchestrator-ip>:5010/service_orchestrator/deploy_service
```
You will need a load testing tool for MQTT to stress the IoT Agent microservice. In this demo, we use the [Locust](https://locust.io/) tool to stress the IoT Agent. See Step 3 to configure the tool. 

### Running the touristic CDN service
**Step 1**: To create a slice as described in touristic_cdn_slice-activator-to-slice-spec-processor.yaml file, located at `/Tenant/Touristic/slice`, enter the following command
```bash
curl -X POST --data-binary @touristic_cdn_slice-activator-to-slice-spec-processor.yaml -H "Content-type: text/x-yaml" http://<slice-activator-ip>:5001>/slice_activator/create_slice
```
As a response to this call, the Slice Activator will receive a yaml file describing the slice created. 

**Step 2**: To deploy the CDN service, run the following commands
```bash
python3 tourCDNdeployment.py slice_creation_response app-data.json 
```
where,

   - tourCDNdeployment.py : This script is executed from the tenant after the slice creation for the Touristic service auto-configuration. It is located at `/Tenant/Touristic/service`
   - app-data.json : Describe the DNS configuration message format. It is located at `/Tenant/Touristic/service`
   - slice_creation_responce: This is the yaml returned to the Slice Activator describing the slice that has been created.

The CDN Web GUI will be available at `http://<dc-slice-controller-at-core-ip>:60080`


