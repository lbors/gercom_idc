from flask import Flask, Blueprint, request
from ruamel.yaml import YAML
import yaml, requests, json
from logs import logs
 
spec_processor = Blueprint('spec_processor', 'spec_processor', url_prefix='/spec_processor')

@spec_processor.route('create_slice', methods=['POST'])
def create_slice():
    # validate json
    print("Validando json no slice_spec_processor para criar slice")
    try:
        logs.logger.info(yaml.load(request.data))
    except Exception as e:
        print("Erro na validação do json no slice_spec_processor para criar slice")
        errors = []
        errors.append("Invalid json file!")
        if(len(e.args)>2): errors.append(e.args[2])
        else: errors.append("expected 'slices:'")
        print("Erro ao criar slice no slice_spec_processor")
        return logs.callback(0, errors), 400
    response = []
    #print(slice_description)
    print("Iniciando comunicação entre spec_processor e slice_builder")
    slice_builder = requests.post('http://localhost:5003/slice_builder/initiate_slice_creation', data=request.data)
       # response.append(slice_builder)
        
    if(slice_builder.status_code == 201):
        print("Sucesso na comunicação entre spec_processor e slice_builder para a criação de slice")
        return logs.callback(1, yaml.load(yaml.load(yaml.dump(slice_builder._content)))), 200
    else:
        print("Falha na comunicação entre spec_processor e slice_builder para a criação de slice")
        return logs.callback(0, slice_builder.status_code), 404 
