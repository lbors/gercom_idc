FROM python:3.7-alpine
RUN \
    apk add git && \
    apk add build-base && \
    apk add openssl-dev libffi-dev && \
    git clone https://github.com/williamgdo/IMA_management.git && \
    pip3 install requests && \
    pip3 install pika==0.13.1 && \
    pip3 install influxdb --default-timeout=100 && \
    pip3 install flask --default-timeout=100 && \
    pip3 install flask_request_params --default-timeout=100 && \
    pip3 install pyyaml --default-timeout=100 && \
    pip3 install paramiko --default-timeout=100 && \
    pip3 install docker --default-timeout=100 && \
    cd IMA_management/ && \
    git checkout refact && \
    git pull

ENTRYPOINT python3.7 IMA_management/code/adapter_ssh.py >> adapter.log
