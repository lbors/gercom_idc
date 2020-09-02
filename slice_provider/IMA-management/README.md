# IMA Management
Installation of IMA Management component tested in Ubuntu 18.04.

The IMA-management component is responsible for interact with different VIMs/WIMs in order to manage the virtual elements and/or services.

The northbound interface communicates the Slice Resource Orchestration with the Resource Engine / Controller in order to start, stop and update the management components. 

The southbound interface communicates the specific Adapters with the respective VIM/WIM. The principal method exercited for this demo is the deploy service and stop service methods. More deitaled information could be found in the Deliverables 5.2 and 6.2.

## Download the source code from gitlab
    $ git clone https://gitlab.com/necos/demos/integrated-demo/edit/addIMA/services/IMA-management/ 

## Install Python3.7
    # Add python repository
    $ sudo add-apt-repository ppa:deadsnakes/ppa
    # Install python version 3.7
    $ sudo apt install -y python3.7
    # Install python3-pip
    $ sudo apt install -y python3-pip

## Install Docker
    $ sudo apt install docker.io

## Install Python Dependencies
    $ python3.7 -m pip install -r IMA-management/requirements.txt

## Run Docker Images for the Adapters components
    $ cd IMA-management
    $ sudo docker build -f Dockerfile -t adapter_ssh:latest .

## Run the IMA management application 
    $ sudo python3.7 IMA-management/code/engine_controller.py
    # Open a new terminal to check if the application starts
    $ curl localhost:5001/
    # The following message will show:
    $ Welcome to Resource and VM Management of IMA!

## Contact
    Authors: André Beltrami <beltrami@ufscar.br>
             William        <williamgdo.app@gmail.com>
