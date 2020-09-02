#!/usr/bin/python3
from flask import Flask, request, jsonify, make_response, abort
import slice_resource_orchestrator as SRO
import elasticity
from config import sro_port
import yaml, sro_logger, copy, time

logger = sro_logger._file('SRO')
app = Flask(__name__)

#########################################################################################
### Error handlers ######################################################################

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

@app.errorhandler(502)
def bad_gateway(error):
    return make_response(jsonify({'Bad Gateway': error.description}), 502)

#########################################################################################
#########################################################################################

# Hello call
@app.route('/', methods=['GET'])
def hello():
    logger.info("[-] Saying Hello!")
    return jsonify({'Status': 'Necos.sro service running'}), 200

# Exit call
@app.route('/bye', methods=['GET'])
def bye():
    shutdown = request.environ.get('werkzeug.server.shutdown')
    if shutdown is None:
        return jsonify({'Error': 'This is not the Werkzeug Server'}), 400
    logger.info("[-] Exit SRO.")
    shutdown()
    return jsonify({'Status': 'Necos.sro is shutting down'}), 200

# Returns the description of the Slice as YAML
@app.route('/necos/sro/slices/<slice_id>', methods=['GET'])
def get_slice(slice_id):
    logger.info(f'[-] Incomming request for slice descriptor of "{slice_id}" (YAML)')
    result, message = SRO.get_slice(slice_id, 'YAML')
    if type(message) is dict:
        return jsonify(message), result
    else:
        return message, result

# Returns the description of the Slice as JSON
@app.route('/necos/sro/slices/<slice_id>/json', methods=['GET'])
def get_slice_json(slice_id):
    logger.info(f'[-] Incomming request for slice descriptor of "{slice_id}" (JSON)')
    result, message = SRO.get_slice(slice_id, 'JSON')
    return jsonify(message), result

# Get all slices as YAML
@app.route('/necos/sro/slices', methods=['GET'])
def get_slices():
    logger.info(f'[-] Incomming request for all slice descriptors (YAML)')
    result, message = SRO.get_slices('YAML')
    if type(message) is dict:
        return jsonify(message), result
    else:
        return message, result

# Get all slices as JSON
@app.route('/necos/sro/slices/json', methods=['GET'])
def get_slices_json():
    logger.info(f'[-] Incomming request for all slice descriptors (JSON)')
    result, message = SRO.get_slices('JSON')
    return jsonify(message), result

def bad_gateway_ima(slice_id, result_ima_mon, result_ima_mgm):
    warning_message = f"    [!] The slice '{slice_id}' was correctly saved in the Slices Database, but "
    bad_gateway_b = False
    if result_ima_mon != 201:
        bad_gateway_b = True
        warning_message += "monitoring part of IMA failed."
    if result_ima_mgm != 200:
        bad_gateway_b = True
        if "monitoring part" in warning_message:
            warning_message = warning_message.replace("part", " and management parts")
        else:
            warning_message += "management part of IMA failed."
    if bad_gateway_b:
        return warning_message

# Stores the Slice details once it has been activated, called by Slice Builder
@app.route('/necos/sro/slices/req_e2e_binding', methods=['POST'])
def create_slice():
    logger.info(f'[-] Performing "Slice Creation"')
    creation_start_time = time.time()
    try:
        content = yaml.load(request.data, Loader=yaml.CLoader)['slices']['sliced']
    except Exception as e:
        logger.error(f'    [X] Invalid YAML file. {e}')
        return abort(400, f'Invalid YAML file. {e}')
    if 'created-slice' in content:
        if content['created-slice'] == True:
            return jsonify({'Continue': 'This call is unnecessary, just send the content of this yaml as response to the call from SRO'}), 202
    SRO.save_file('create_slice', yaml.dump({'slices': {'sliced': content}}, Dumper=yaml.CDumper, width=1000), 'received')
    # Step 13: Interact with DC/WAN Slice controllers to bind the slice 
    # parts together. This completes the topology.
    ''' Ommited because is done by the WAN Controller.'''
    # Step 14: Keep a representation of the slice topology (via Slices DB)
    slicesdb_time = time.time()
    result, message = SRO.store_slice(content)
    slice_id = content['id']
    SRO.save_times(slice_id, '[*] Create Slice', 'w')
    SRO.save_times(slice_id, f'   - Slices Database: {(time.time()-slicesdb_time)*1000} ms.')
    if result != 201:
        logger.error(f'    [X] Error while storing slice descriptor. {message}')
        return jsonify(message), result
    slice_ima_mon = copy.deepcopy(message)
    slice_ima_mgm = copy.deepcopy(message)
    slice_id = message['slice']['id']
    # # Step 15: Inform IMA of the allocated VIMs/WIMs and endpoints.
    ima_time = time.time()
    result_ima_mon, message_ima_mon = SRO.register_slice_at_ima_monitoring(slice_ima_mon['slice'])
    SRO.save_times(slice_id, f'   - IMA.monitoring: {(time.time()-ima_time)*1000} ms.')
    ima_time = time.time()
    result_ima_mgm, message_ima_mgm = SRO.register_slice_at_ima_management(slice_ima_mgm['slice'])
    SRO.save_times(slice_id, f'   - IMA.management: {(time.time()-ima_time)*1000} ms.')
    warning_message = bad_gateway_ima(slice_id, result_ima_mon, result_ima_mgm)
    SRO.save_times(slice_id, f'[-] Total time for slice creation: {(time.time()-creation_start_time)*1000} ms.')
    if warning_message:
        logger.warning(warning_message)
        return abort(502, warning_message)
    else:
        logger.info(f"[-] Slice '{slice_id}' was correctly saved. It is now monitored and managed by IMA.")
        return jsonify({'slice_id': slice_id}), result
    # return jsonify({'ok': 'saved'}), 201 # comment 

# Called by Service Orchestrator Adaptor
# Receives the slice_id and the commands to deploy the service on the vdus
# and the namespace parameter for IMA to monitor
@app.route('/necos/sro/deploy_service', methods=['POST'])
def deploy_service():
    logger.info(f'[-] Performing "Service Deployment"')
    service_start_time = time.time()
    try:
        content = yaml.load(request.data, Loader=yaml.CLoader)
    except Exception as e:
        logger.error(f'    [X] Invalid YAML file. {e}')
        return abort(400, f'Invalid YAML file. {e}')
    SRO.save_file('deploy_service', yaml.dump(content, Dumper=yaml.CDumper, width=1000), 'received')
    slice_id = content['slices']['sliced']['id']
    SRO.save_times(slice_id, '[*] Service deployment')
    if not SRO.check_if_slice_exists(slice_id):
        return abort(404, f'The slice \'{slice_id}\' does not exists')
    elasticity_content = copy.deepcopy(content)
    ima_mgm_content = copy.deepcopy(content)
    ima_mon_content = copy.deepcopy(content)
    # Call left side of IMA to deploy the service
    ima_time = time.time()
    result_ima_mgm, message_ima_mgm = SRO.deploy_service(ima_mgm_content)
    SRO.save_times(slice_id, f'   - IMA.management: {(time.time()-ima_time)*1000} ms.')
    if result_ima_mgm != 200:
        logger.error(f'    [X] Management part of IMA failed"')
        return abort(502, f'Management part of IMA failed: {message_ima_mgm}')
    # Call right side of IMA to start monitoring services
    # ima_time = time.time()
    # result_ima_mon, message_ima_mon = SRO.monitor_service(ima_mon_content)
    # SRO.save_times(slice_id, f'   - IMA.monitoring: {(time.time()-ima_time)*1000} ms.')
    # if result_ima_mon != 201:
    #     logger.error(f'    [X] Monitoring part of IMA failed"')
    #     return abort(502, f'Monitoring part of IMA failed: {message_ima_mon}')
    logger.info(f"    [-] The requested services are running and monitored")
    # Elasticity
    elasticity_time = time.time()
    need_elasticity = SRO.save_elasticity_policies(elasticity_content)
    if need_elasticity:
        elasticity.start(elasticity_content['slices']['sliced']['id'])
    SRO.save_times(slice_id, f'   - Save elasticity policies and start elasticity agents: {(time.time()-elasticity_time)*1000} ms.')
    SRO.save_times(slice_id, f'[-] Total time for service deployment: {(time.time()-service_start_time)*1000} ms.')
    return jsonify({slice_id: 'The requested services are running and monitored.'}), 200

# Called by Slice Activator
# Receives a YAML with the slice_id and calls DC/WAN controllers,
# IMA, and Slices Database to delete the slice.
@app.route('/necos/sro/delete_slice', methods=['DELETE'])
def slice_decommission():
    logger.info(f'[-] Performing "Slice Decommission"')
    decommission_start_time = time.time()
    try:
        content = yaml.load(request.data, Loader=yaml.CLoader)['slices']['sliced']
    except Exception as e:
        logger.error(f'    [X] Invalid YAML file. {e}')
        return abort(400, f'Invalid YAML file. {e}')
    SRO.save_file('slice_decommission', yaml.dump({'slices': {'sliced': content}}, Dumper=yaml.CDumper, width=1000), 'received')
    slice_id = content['slice-id']
    SRO.save_times(slice_id, '[*] Slice decommission')
    if not SRO.check_if_slice_exists(slice_id):
        return abort(404, f'The slice \'{slice_id}\' does not exists')
    result, message = SRO.delete_slice(slice_id)
    SRO.save_times(slice_id, f'[-] Total time for slice decommission: {(time.time()-decommission_start_time)*1000} ms.')
    return jsonify(message), result

# def start_elasticity():
#     slices = get_slices_json()[0]['slices']
#     for Slice in slices:
        
#     if need_elasticity:
#         elasticity.start(elasticity_content['slices']['sliced']['id'])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=sro_port, debug=True, use_reloader=False)