from config import slices_db_url, ima_mon_url, ima_mgm_url, json_header, yaml_header, text_header
import yaml, requests, logging, time
from ast import literal_eval
from threading import Thread

logger = logging.getLogger('Necos.SRO')

def check_if_slice_exists(slice_id):
    logger.info(f'    - Checking if slice "{slice_id}" exists')
    r = requests.get(f'{slices_db_url}/slices/exists/{slice_id}')
    if r.status_code == 200:
        return True
    else: 
        return False

def get_slice(slice_id, _type):
    r = requests.get(f'{slices_db_url}/slices/{slice_id}')
    if r.status_code == 200:
        if _type == 'JSON':
            return r.status_code, r.json()
        elif _type == 'YAML':
            if r.json().get('slice'):
                message = yaml.dump(r.json(), Dumper=yaml.CDumper, width=1000)
            else:
                message = r.json()
            return r.status_code, message
        else:
            return 500, f'Unknown format "{_type}"'
    else:
        try:
            message = r.json()
        except:
            message = r.text
        return r.status_code, message

def get_slices(_type):
    r = requests.get(f'{slices_db_url}/slices')
    if r.status_code == 200:
        if _type == 'JSON':
            return r.status_code, r.json()
        elif _type == 'YAML':
            if r.json().get('slices'):
                message = yaml.dump(r.json(), Dumper=yaml.CDumper, width=1000)
            else:
                message = r.json()
            return r.status_code, message
        else:
            return 500, f'Unknown format "{_type}"'
    else:
        try:
            message = r.json()
        except:
            message = r.text
        return r.status_code, message

def store_slice(content):
    logger.info('    - Saving slice in Slices Database')
    r = requests.post(f'{slices_db_url}/slices', json = content, headers = json_header)
    return r.status_code, r.json()

def save_file(file_name, content, folder='generated'):
    f = open(f'{folder}/{file_name}.yaml', 'w')
    f.write(content)
    f.close()

def register_slice_at_ima_monitoring(content):
    logger.info('    - Registering slice at IMA (monitoring)')
    logger.info('        - Generating YAML')
    for section in ['created-slice', 'logo', 'slice-constraints', 'slice-requirements', 'slice-lifecycle', 'cost']:
        content.pop(section, None)
    net_parts = []
    for slice_part in content['slice-parts']:
        _type = next(iter(slice_part.keys()))
        slice_part_sections = next(iter(slice_part.values()))
        if _type == 'net-slice-part':
            net_parts.append(slice_part)
            continue
        for section in ['cost', 'dc-slice-controller', 'VIM']:
            slice_part_sections.pop(section, None)
    for net_part in net_parts:
        content['slice-parts'].remove(net_part)
    yaml_stream = yaml.dump({'slice': content}, Dumper=yaml.CDumper, width=1000)
    save_file('deploy_slice_ima_mon', yaml_stream)
    logger.info('        - Calling IMA.monitoring')
    r = requests.post(f'{ima_mon_url}/start_monitoring', headers = yaml_header, data = yaml_stream)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text
    logger.info(f'        - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    return r.status_code, message

def register_slice_at_ima_management(content):
    logger.info('    - Registering slice at IMA (management)')
    logger.info('        - Generating YAML')
    Slice = {'slice': {'id': content['id'], 'slice-parts': content['slice-parts']}}
    net_parts = []
    for slice_part in Slice['slice']['slice-parts']:
        _type = next(iter(slice_part.keys()))
        if _type == 'net-slice-part':
            net_parts.append(slice_part)
            continue
        for section in ['dc-slice-part-id', 'type', 'cost', 'dc-slice-controller', 'monitoring-parameters']:
            slice_part['dc-slice-part'].pop(section)
        for section in ['version', 'vim-shared', 'vim-federated', 'host-count', 'vswitch']:
            slice_part['dc-slice-part']['VIM'].pop(section)
        for vdu in slice_part['dc-slice-part']['VIM']['vdus']:
            for section in ['instance-count', 'hosting', 'epa-attributes']:
                vdu['vdu'].pop(section)
    for net_part in net_parts:
        Slice['slice']['slice-parts'].remove(net_part)
    yaml_stream = yaml.dump(Slice, Dumper=yaml.CDumper, width=1000)
    save_file('deploy_slice_ima_mgm', yaml_stream)
    logger.info('        - Calling IMA (management)')
    r = requests.post(f'{ima_mgm_url}/start_management', headers = yaml_header, data = yaml_stream)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text #{'Unexpected message': r.text}   
    logger.info(f'        - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    return r.status_code, message

def deploy_service(content):
    logger.info('    - Generating YAML for IMA.management')
    for part in content['slices']['sliced']['slice-parts']:
        try:
            part.pop('elasticity')
        except KeyError:
            pass
    yaml_stream = yaml.dump(content, Dumper=yaml.CDumper, width=1000)
    save_file('deploy_service_ima_mgm', yaml_stream)
    logger.info('    - Calling IMA.management to deploy the service')
    r = requests.post(f'{ima_mgm_url}/deploy_service', headers = yaml_header, data = yaml_stream)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text
    logger.info(f'    - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    return r.status_code, message
    return 200, 'ok'

def save_elasticity_policies(content):
    logger.info(f'    - Looking for elasticity policies.')
    slice_id = content['slices']['sliced']['id']
    policies = 0
    try:
        for part in content['slices']['sliced']['slice-parts']:
            part_name = part['name']
            if 'elasticity' in part:
                for policy in part['elasticity']:
                    # cal slicesDB to save policy
                    r = requests.post(f'{slices_db_url}/{slice_id}/{part_name}/elasticity_policy', headers = json_header, json = policy)
                    if r.status_code != 201:
                        logger.warning(f'An elasticity policy could not be saved. {r.text}')
                    policies += 1
    except:
        logger.info(f'       [X] There were problems when checking for elasticity.')
        return False
    if policies:
        logger.info(f'       - {policies} elasticity policies saved.')
        return True
    else:
        logger.info(f'       - This slice does not require elasticity.')
        return False

def monitor_service(content):
    content = content['slices']['sliced']
    logger.info('    - Adding namespaces to Slices Database')
    for part in content['slice-parts']:
        part_name = part['name']
        namespace = part['vdus'][0]['namespace']
        slice_id = content['id']
        r = requests.put(f'{slices_db_url}/{slice_id}/{part_name}/add_namespace', headers = json_header, json = {'namespace': namespace})
        if r.status_code != 201:
            logger.warning('A namespace could not be saved.')
    logger.info(f'    - Requesting slice \'{content["id"]}\' from Slices Database')
    result, Slice = get_slice(content['id'], 'JSON')
    Slice = Slice['slice']
    logger.info('    - Generating YAML for IMA.monitoring')
    for section in ['logo', 'slice-constraints', 'slice-requirements', 'slice-lifecycle', 'cost']:
        Slice.pop(section)
    for slice_part in Slice['slice-parts']:
        _type = next(iter(slice_part.keys()))
        slice_part_sections = next(iter(slice_part.values()))
        if _type == 'net-slice-part':
            net_part = slice_part
            continue
        for section in ['cost', 'dc-slice-controller', 'VIM']:
            slice_part_sections.pop(section)
    Slice['slice-parts'].remove(net_part)
    yaml_stream = yaml.dump({'slice': Slice}, Dumper=yaml.CDumper, width=1000)
    save_file('deploy_service_ima_mon', yaml_stream)
    logger.info('    - Calling IMA.monitoring to monitor containers')
    r = requests.post(f'{ima_mon_url}/start_container_monitoring', headers = yaml_header, data = yaml_stream)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except Exception as e:
        message = r.text #{'Unexpected message': r.text}
    logger.info(f'    - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    return r.status_code, message

def save_times(Slice, line, action='a'):
    f = open('generated/times'+Slice, action)
    f.write(f'{line}\n')
    f.close()

def call_controller_delete(controller, ip, port, part, yaml_stream, res):
    logger.info(f'        - Calling controller {controller} at \'http://{ip}:{port}\' to remove part {part}')
    save_file(f'controller_decommission_{part}', yaml_stream)
    controller_url = f'http://{ip}:{port}'
    # controller_url = ima_mon_url
    # time.sleep(3)
    r = requests.delete(f'{controller_url}/slice_part', headers = yaml_header, data = yaml_stream)
    if (r.status_code == 201) or (r.status_code == 200):
        code = 200
    else:
        code = 500
    res.append(code)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text #{'Unexpected message': r.text}
    logger.info(f'        - Response (Controller {controller}) | Status code[{r.status_code}]: {str(message)[0:100]}...')

def delete_slice(slice_id):
    slice_part_ids = []
    errors_in_controllers, errors_in_ima_mon, errors_in_ima_mgm, errors_in_slices_db = False, False, False, False
    # Calls for DC/WAN Controllers
    logger.info('    - Looking for slice part ids')
    r = requests.get(f'{slices_db_url}/slice_part_ids/{slice_id}/all')
    if r.status_code != 200:
        return 404, r.json()
    slice_part_ids = [{'slice-controller-id': int(x.split('-')[0]), 'uuid': int(x.split('-')[1])} for x in r.json()['slice_part_ids']]
    calls = []
    calls_res = []
    logger.info('    - Calling DC/WAN Controllers')
    for part_id in slice_part_ids:
        r = requests.get(f'{slices_db_url}/controller_pointer/{part_id["slice-controller-id"]}')
        if r.status_code != 200:
            return 500, r.json()
        controller = r.json()['controller']
        yaml_stream = yaml.dump({'slices': {'sliced': {'slice-id': slice_id, 'slice-part-id': part_id}}}, Dumper=yaml.CDumper, width=1000)
        calls.append(Thread(target=call_controller_delete, kwargs={'controller': part_id["slice-controller-id"],'ip': controller["ip"], 'port': controller["port"], 'part': part_id["uuid"], 'yaml_stream': yaml_stream, 'res': calls_res}))
    controllers_time = time.time()
    for call in calls:
        call.start()
    for call in calls:
        call.join()
    save_times(slice_id, f'   - DC/WAN Controllers (Parallel): {(time.time()-controllers_time)*1000} ms.')
    if any(res != 200 for res in calls_res):
        errors_in_controllers = True
    # Call for IMA monitoring
    logger.info('    - Calling IMA.monitoring')
    ima_time = time.time()
    r = requests.get(f'{slices_db_url}/slice_part_ids/{slice_id}/dc')
    save_times(slice_id, f'   - IMA.monitoring: {(time.time()-ima_time)*1000} ms.')
    if r.status_code != 200:
        return 404, r.json()
    slice_part_ids = [{'dc-slice-part': {'dc-slice-part-id': {'slice-controller-id': int(x.split('-')[0]), 'slice-part-uuid': int(x.split('-')[1])}}} for x in r.json()['slice_part_ids']]
    yaml_stream = yaml.dump({'slice': {'id': slice_id, 'slice-parts': slice_part_ids}}, Dumper=yaml.CDumper, width=1000)
    save_file(f'decommission_ima_mon', yaml_stream)
    r = requests.post(f'{ima_mon_url}/delete_monitoring', headers = yaml_header, data = yaml_stream)
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text #{'Unexpected message': r.text}
    logger.info(f'    - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    if (r.status_code != 201) and (r.status_code != 200):
        errors_in_ima_mon = True
    # Call for IMA management
    logger.info('    - Calling IMA.management')
    ima_time = time.time()
    r = requests.post(f'{ima_mgm_url}/stop_management', headers=text_header, data=slice_id)
    save_times(slice_id, f'   - IMA.management: {(time.time()-ima_time)*1000} ms.')
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text #{'Unexpected message': r.text}
    logger.info(f'    - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    if r.status_code != 200:
        errors_in_ima_mgm = True
    # Call for Slices Database
    logger.info('    - Calling Slices Database to remove the slice')
    slicesdb_time = time.time()
    r = requests.delete(f'{slices_db_url}/slices/{slice_id}')
    save_times(slice_id, f'   - Slices Database: {(time.time()-slicesdb_time)*1000} ms.')
    try:
        message = r.json()
        if type(message) is str:
            message = literal_eval(message)
    except:
        message = r.text 
    logger.info(f'    - Response | Status code[{r.status_code}]: {str(message)[0:100]}...')
    if r.status_code != 200:
        errors_in_slices_db = True
    if any([errors_in_controllers, errors_in_ima_mon, errors_in_ima_mgm, errors_in_slices_db]):
        components = ['DC/WAN Controller(s)', 'IMA.monitoring', 'IMA.management', 'Slices Database']
        failures = [components[i] for i, val in enumerate([errors_in_controllers, errors_in_ima_mon, errors_in_ima_mgm, errors_in_slices_db]) if val] 
        response = {'Error': f'There were errors in the following components: {failures}'}
        logger.warning(f'[X] There were errors in the following components: {failures}')
        status_code = 500
    else:
        response = {'Deleted': f'The slice \'{slice_id}\' was correctly removed.'}
        logger.info(f'[-] The slice \'{slice_id}\' was correctly removed.')
        status_code = 200
    return status_code, response

def update_slice(slice_id, content):
    logger.info(f'    - Updating the slice description in Slices Database')
    r = requests.put(f'{slices_db_url}/slices/{slice_id}', json = content, headers = json_header)
    return r.status_code, r.json()