import logging
import random
import uuid, json
from settings import *
from network_manager.ovs_handler import *
from slice_creator import logs

def create_resource(slice_part_id):
    bridge_name = "br_" + str(slice_part_id)

    logs.logger.info(create_bridge(bridge_name))

    return {"bridge-name": bridge_name, "type": "openvswitch"}

def remove_resource(slice_part_id):

    bridge_name = "br_" + str(slice_part_id)
    delete_bridge(bridge_name)
    return 1
