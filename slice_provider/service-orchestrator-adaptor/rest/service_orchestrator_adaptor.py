from flask import Flask, Blueprint, request
import yaml, requests
from logs import logs
from settings import SRO
 
soa = Blueprint('soa', 'soa', url_prefix='/soa')


@soa.route('/deploy_service', methods=['POST'])
def deploy_service():
    sro = requests.post('http://' + SRO + '/necos/sro/deploy_service', data=request.data)
        
    if sro.status_code == 200:
        return logs.callback(1, yaml.load(yaml.load(yaml.dump(sro._content)))), 200
    else:
        return logs.callback(0, sro.status_code), 404
