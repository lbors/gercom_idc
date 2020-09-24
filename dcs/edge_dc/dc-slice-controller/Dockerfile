FROM ubuntu:18.04


ENV PYTHON_VERSION="3.6.8"
ENV DEBIAN_FRONTEND noninteractive

# system dependencies
RUN apt-get update \
  && apt-get install -y software-properties-common build-essential wget cmake zlib1g-dev libssl-dev

# libvirt dependencies
RUN apt-get update \
  && apt-get install -y libvirt-dev virtinst


# install Python 3.6.X
RUN wget https://www.python.org/ftp/python/${PYTHON_VERSION}/Python-${PYTHON_VERSION}.tar.xz

RUN tar xvf Python-${PYTHON_VERSION}.tar.xz \
    && rm Python-${PYTHON_VERSION}.tar.xz \
    && cd Python-${PYTHON_VERSION} \
    && ./configure \
    && make altinstall \
    && cd / \
    && rm -rf Python-${PYTHON_VERSION}

# mysql dependencies
RUN apt-get install -y python3-dev libmysqlclient-dev

# install and update pip
RUN wget https://bootstrap.pypa.io/get-pip.py | python3.6 \
    && pip3.6 install --upgrade pip \
    && rm get-pip.py


COPY requirements.txt /mnt/requirements.txt
COPY Code /mnt/Code

RUN pip3.6 install -r /mnt/requirements.txt

WORKDIR /mnt/Code

# ssh client
RUN apt-get install -y openssh-client

ENTRYPOINT python3.6 run.py




