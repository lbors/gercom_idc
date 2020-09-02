from flask import Flask, Blueprint, request
from ruamel.yaml import YAML
import yaml, json, requests
from logs import logs
from settings import SSP_IP, SRO_IP
 
slice_activator = Blueprint('slice_activator', 'slice_activator', url_prefix='/slice_activator')

@slice_activator.route('create_slice', methods=['POST'])
def create_slice():
    # validate yaml
    print("Iniciando criação de slice no slice_activator")
    spec_processor = requests.post('http://'+SSP_IP+':5002/spec_processor', data=request.data)
    if(spec_processor.status_code == 201 ):
        print("Comunicação com spec_processor estabelecida e slice criada")
        return logs.callback(1, spec_processor.status_code), 201
    else:
        print("Erro ao comunicar com spec_processor")
        return logs.callback(0, spec_processor.status_code), 404

@slice_activator.route('delete_slice', methods=['POST'])
def delete_slice():
    # validate yaml
    print("Iniciando remoção de slice no slice_activator")
    sro_response = requests.post('http:'+SRO_IP+':5005/necos/sro/slices/req_e2e_binding', data=request.data)

    if(sro_response.status_code == 201 or sro_response.status_code == 200):
        print("Comunicação com spec_processor estabelecida e slice deletada")
        return logs.callback(1, sro_response._content), 201
    else:
        print("Erro ao comunicar com spec_processor")
        return logs.callback(0, sro_response._content), 404
    
