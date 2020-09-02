from flask import Flask, jsonify, request
from ruamel.yaml import YAML
from rest.slice_activator import slice_activator

app = Flask(__name__)
    
app.register_blueprint(slice_activator)

app.run(host='0.0.0.0', port=5001, debug=True)