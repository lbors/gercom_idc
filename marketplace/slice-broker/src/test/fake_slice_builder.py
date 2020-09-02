import requests
import json
import sys
import yaml

#use the file "yaml/yaml-files_dojot_iot_slice-builder-to-slice-broker.yaml"

if len(sys.argv) != 3:
    print("Use: python3 slice_builder.py BROKER_IP:BROKER_PORT file_name.yaml")
    exit (0)

BROKER_IP_PORT = sys.argv[1]
YAML_FILE = sys.argv[2]

with open(YAML_FILE, 'rb') as wgetrc_handle:
    response = requests.post('http://'+BROKER_IP_PORT+'/locate_slice_resources', files={'pdt':wgetrc_handle})

yaml_response = yaml.load(response.text)

print(yaml_response)

# recebimento da msg PDT, através de uma msg SRA, com os campos da requisição preenchidos.
