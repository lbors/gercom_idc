import requests
import yaml
import time
from flask import Blueprint, request
from logs import logs
from datetime import datetime
from settings import SOA

service_orchestrator = Blueprint('service_orchestrator', 'service_orchestrator', url_prefix='/service_orchestrator')


@service_orchestrator.route('/deploy_service', methods=['POST'])
def deploy_service():
    start = time.time()
    now = datetime.now()
    soa = requests.post('http://' + SOA + '/soa/deploy_service', data=request.data)
    if soa.status_code == 200:
        dt_string = now.strftime("%d/%m/%Y %H:%M:%S")
        end = time.time()
        logs.echo_to_file("Call made at " + dt_string + ". Time spent: " + str(end - start) + " seconds.")
        return logs.callback(1, yaml.load(yaml.load(yaml.dump(soa._content)))), 200
    else:
        return logs.callback(0, soa.status_code), 404
