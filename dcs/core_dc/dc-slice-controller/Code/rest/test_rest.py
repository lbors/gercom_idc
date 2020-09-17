from flask import Flask, Blueprint, request, jsonify
from model.template import Template
from slice_creator import validate_yaml, logs
import json
import yaml

server_test = Blueprint('server_test', 'server_test', url_prefix='/')

@server_test.route('', methods=['GET'])
def get():
    
    return jsonify({"result" : "DCSC webserver is up and running"}), 200