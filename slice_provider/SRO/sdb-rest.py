#!/usr/bin/python3
from flask import Flask, request, jsonify, make_response, abort
import slices_database as SDB
from config import slices_db_port
from threading import Thread
import yaml, sro_logger, os, time

logger = sro_logger._file('SDB')

app = Flask(__name__)

### Error handlers ###
@app.errorhandler(400)
def bad_request(error):
    return make_response(jsonify({'Bad Request': error.description}), 400)

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'Not Found': error.description}), 404)

@app.errorhandler(409)
def conflict(error):
    return make_response(jsonify({'Conflict': error.description}), 409)

@app.errorhandler(500)
def internal_server_error(error):
    return make_response(jsonify({'Internal Server Error': error.description}), 500)

# Hello call
@app.route('/', methods=['GET'])
def hello():
    logger.info("[-] Saying Hello!")
    return jsonify({'Status': 'Necos.slicesdb service running'}), 200

    # Exit call
@app.route('/bye', methods=['GET'])
def bye():
    shutdown = request.environ.get('werkzeug.server.shutdown')
    if shutdown is None:
        return jsonify({'Error': 'This is not the Werkzeug Server'}), 400
    logger.info("[-] Exit Slices Database.")
    shutdown()
    return jsonify({'Status': 'Necos.slices_database is shutting down'}), 200

# Store the Slice details once it is activated
@app.route('/necos/db/slices', methods=['POST'])
def store_slice():
    result, message = SDB.store_slice(request.get_json())
    if result == 201:
        return jsonify(message), result
    else:
        return abort(result, message)

# Check if a slice exists
@app.route('/necos/db/slices/exists/<slice_id>', methods=['GET'])
def check_if_slice_exists(slice_id):
    result, message = SDB.slice_exists(slice_id)
    return jsonify(message), result

# Updates the Slice details once it is activated
@app.route('/necos/db/slices/<slice_id>', methods=['PUT'])
def update_slice(slice_id):
    result, message = SDB.update_slice(slice_id, request.get_json())
    if result == 201:
        return jsonify(message), result
    else:
        return abort(result, message)

# Get the details of a Slice as JSON
@app.route('/necos/db/slices/<slice_id>', methods=['GET'])
def get_slice(slice_id):
    result, message = SDB.get_slice(slice_id)
    if result == 200:
        return jsonify(message), result
    else:
        return abort(result, message)

# Create auxiliar node
@app.route('/necos/db/auxiliar', methods=['POST'])
def aux_node():
    result, message = SDB.aux_node()
    if result == 201:
        return jsonify({'Created': message}), result
    else:
        return abort(result, message)

# Delete the slice
@app.route('/necos/db/slices/<slice_id>', methods=['DELETE'])
def delete_slice(slice_id):
    result, message = SDB.delete_slice(slice_id)
    if result == 200:
        return jsonify({'Deleted': message}), result
    else:
        return abort(result, message)

# Get the slice_part_ids of a Slice
@app.route('/necos/db/slice_part_ids/<slice_id>/<_type>', methods=['GET'])
def get_slice_parts(slice_id, _type):
    result, message = SDB.slice_exists(slice_id)
    if result == 404:
        return abort(404, f'The slice \'{slice_id}\' does not exists')
    result, message = SDB.get_slice_part_ids(slice_id, _type)
    if result == 200:
        return jsonify({'slice_part_ids': message}), result
    else:
        return abort(result, message)

@app.route('/necos/db/controller_pointer/<int:controller_id>', methods=['GET'])
def get_controller_pointer(controller_id):
    result, message = SDB.get_controller_pointer(controller_id)
    if result == 200:
        return jsonify({'controller': message}), result
    else:
        return abort(result, message)

# Get all slices
@app.route('/necos/db/slices', methods=['GET'])
def get_slices():
    result, message = SDB.get_slices()
    if result == 200:
        return jsonify({'slices': message}), result
    else:
        return abort(result, message)

# Store the information of a controller
@app.route('/necos/db/register_controller', methods=['POST'])
def register_controller():
    result, message = SDB.register_controller(request.data)
    if result == 201:
        return jsonify({'Created': message}), result
    else:
        return abort(result, message)

# Store the adaptors for API and SSH
@app.route('/necos/db/<slice_id>/<part_name>/add_namespace', methods=['PUT'])
def add_namespace(slice_id, part_name):
    namespace = request.get_json()
    result, message = SDB.add_namespace(slice_id, part_name, namespace['namespace'])
    return jsonify({'result': message}), result

# Store elasticity policy in the database
@app.route('/necos/db/<slice_id>/<part_name>/elasticity_policy', methods=['POST'])
def elasticity_policy(slice_id, part_name):
    policy = request.get_json()
    result, message = SDB.add_elasticity_policy(slice_id, part_name, policy['policy'])
    return jsonify({'result': message}), result

# get elasticity policy
@app.route('/necos/db/<slice_id>/<part_id>/elasticity', methods=['GET'])
def get_elasticity_policy(slice_id, part_id):
    result, message = SDB.get_elasticity_policy(slice_id, part_id)
    if result == 200:
        return jsonify(message), result
    else:
        return abort(result, message)

# get core part
@app.route('/necos/db/<slice_id>/core', methods=['GET'])
def get_core_part(slice_id):
    result, message = SDB.get_core_part(slice_id)
    if result == 200:
        return jsonify(message), result
    else:
        return abort(result, message)

# get vdus
@app.route('/necos/db/<slice_id>/<part_id>/vdus', methods=['GET'])
def get_vdus(slice_id, part_id):
    result, message = SDB.get_vdus_id(slice_id, part_id)
    if result == 200:
        return jsonify({'vdus': message}), result
    else:
        return abort(result, message)

# Save a VDU
@app.route('/necos/db/<slice_id>/<part_name>/vdu', methods=['POST'])
def add_VDU(slice_id, part_name):
    vdu = request.get_json()
    print(vdu)
    vdu_id = SDB.store_new_vdu(slice_id, part_name, vdu)
    return jsonify({'vdu_id': vdu_id}), 201

# get granularity
@app.route('/necos/db/<slice_id>/<part_id>/monitor/granularity', methods=['GET'])
def get_monitor_granularity(slice_id, part_id):
    result, message = SDB.get_monitor_granularity(slice_id, part_id)
    if result == 200:
        return jsonify(message), result
    else:
        return abort(result, message)

def startup():
    time.sleep(10)
    os.system('python3 clean_slices_db.py')
    print('SlicesDB started...', flush=True)

if __name__ == '__main__':
    Thread(target=startup).start()
    app.run(host='0.0.0.0', port=slices_db_port, debug=True, use_reloader=False)