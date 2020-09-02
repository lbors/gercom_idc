import logging
import random
import socket

from flask import Flask
from flask import request, jsonify

from ovs_handler import *

app = Flask(__name__)


if __name__ != '__main__':
    gunicorn_logger = logging.getLogger("gunicorn.error")
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)



@app.route("/")
def hello():
    result = show_info()
    
    return jsonify({"result" : "wan agent rest service is up and running",
    "message" : result})


@app.route("/resource")
def show_resource():
    bridge_name = request.args.get("bridge_name")

    #result = has_bridge(bridge_name)
    r = show_info()
    app.logger.info(r)

    return jsonify({"result" : True})


@app.route("/create_tunnel")
def create_tunnel():
    tunnel_key = request.args.get("tunnel_key")
    bridge_name = request.args.get("bridge_name")
    remote_address = request.args.get("remote_address")
    
    result = set_vxlan(bridge_name, remote_address, tunnel_key)

    if result:
        result = "created"
    else:
        result = "not_created"

    return jsonify({"result" : result})


@app.route("/remove_tunnel")
def remove_tunnel():
    tunnel_key = request.args.get("tunnel_key")
    bridge_name = request.args.get("bridge_name")

    result = remove_vxlan(bridge_name, tunnel_key)

    if result:
        result = "removed"
    else:
        result = "not_removed"

    return jsonify({"result" : result})


if __name__ == "__main__":
    app.config["DEBUG"] = True
    app.run(host='0.0.0.0', port=8080)