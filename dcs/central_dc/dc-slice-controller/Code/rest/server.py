from flask import Flask, jsonify, request
from rest.user_rest import user
from rest.vim_type_rest import vim_type
from rest.cpu_rest import cpu
from rest.controller_rest import controller
from rest.vim_rest import vim
from rest.template_rest import template
from rest.host_rest import host
from rest.slice_part_rest import slice_part
from rest.vm_rest import vm
from rest.test_rest import server_test
from functools import wraps
from ruamel.yaml import YAML
import time

app = Flask(__name__)
    
app.register_blueprint(user)
app.register_blueprint(vim_type)
app.register_blueprint(cpu)
app.register_blueprint(controller)
app.register_blueprint(vim)
app.register_blueprint(template)
app.register_blueprint(host)
app.register_blueprint(slice_part)
app.register_blueprint(vm)
app.register_blueprint(server_test)


app.run(host='0.0.0.0', port=5000, debug=True)