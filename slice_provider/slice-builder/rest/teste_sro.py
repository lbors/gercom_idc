from flask import Flask, Blueprint, request
from ruamel.yaml import YAML
import yaml, json, requests, re, _thread, time
from logs import logs
from settings import SRO_IP

teste_sro = Blueprint('teste_sro', 'teste_sro', url_prefix='/teste_sro')

@teste_sro.route('', methods=['POST'])
def initiate_slice_creation():
    file = open('./Yaml/builder-to-sro-2.yaml', 'r') 
    file_yaml = yaml.load(yaml.dump(file.read()))
    sro_response = requests.post('http://'+SRO_IP+':5005/necos/sro/slices/req_e2e_binding', data=file_yaml)
    logs.logger.info(sro_response)