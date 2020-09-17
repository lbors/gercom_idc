FROM ubuntu:16.04


# system dependencies
RUN apt-get update \
  && apt-get install -y python3-pip python3-dev \
  && apt-get install -y build-essential libssl-dev libffi-dev python3-setuptools \
  && pip3 install --upgrade pip


#ovs install
RUN apt-get install -y openvswitch-switch

# app dependencies
COPY ./requirements.txt /mnt/requirements.txt

RUN pip3 install -r /mnt/requirements.txt

COPY ./src /mnt/src
COPY ./run_server.sh /mnt/run_server.sh


ENTRYPOINT ["/bin/bash", "/mnt/run_server.sh"]
