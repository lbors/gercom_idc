from flask import Flask, jsonify, request
from ruamel.yaml import YAML
from rest.slice_spec_processor import spec_processor

app = Flask(__name__)
    
app.register_blueprint(spec_processor)

app.run(host='0.0.0.0', port=5002, debug=True)