import logging, re
from ast import literal_eval

logger = logging.getLogger('Necos.SRO')

def check_int(s):
    try:
        return int(s)
    except ValueError as e:
        return s

def check_for_struct(s):
    if s.startswith('[') and s.endswith(']'):
        value = [check_int(x) for x in s[1:-1].split(', ')]
    elif s.startswith('{') and s.endswith('}'):
        value = {}
        s = s[1:-1].split(': ')
        while s:
            key = check_int(s.pop(0))
            v = check_int(s.pop(0))
            value[key] = v
    else:
        return s
    return value

def to_camelCase(property):
    camelCaseProperty = ''
    for word in property.split('-'):
        if not camelCaseProperty:
            camelCaseProperty += word
        else:
            camelCaseProperty += word.capitalize()
    return camelCaseProperty

def to_underscore(property):
    words = re.findall('[a-zA-Z][^A-Z]*', property) 
    property = '-'.join([w.lower() for w in words]) 
    return property


def get_parameters(data):
    aux = '{'
    for key, value in data.items():
        if type(value) is str:
            value = value.replace('\'', '')
            aux += f'{to_camelCase(key)}: \'{value}\', '
        elif type(value) is None:
            value = 'null'
            value = value.replace('\'', '')
            aux += f'{to_camelCase(key)}: \'{value}\', '
        else:
            aux += f'{to_camelCase(key)}: {value}, '
    data = aux[:-2] + '}'
    return data

def extract_slice_from_json(content):
    try:
        constraints = content.pop('slice-constraints', None)
        if constraints:
            for key, value in constraints.items():
                if key == 'geographic':
                    content[f'constraint-{key}'] = str(value)
                else:
                    content[f'constraint-{key}'] = value
        requirements = content.pop('slice-requirements', None)
        if requirements:
            content['requirement-elasticity'] = requirements['elasticity']
            for key, value in requirements['reliability'].items():
                content[f'requirement-reliability-{key}'] = value
        lifecycle = content.pop('slice-lifecycle', None)
        if lifecycle:
            for key, value in lifecycle.items():
                content[f'lifecycle-{key}'] = value
        cost = content.pop('cost', None)
        if cost:
            dc_model = cost.pop('dc-model', None)
            net_model = cost.pop('net-model', None)
            if dc_model:
                for key, value in dc_model.items():
                    content[f'cost-dc-{key}'] = str(value)
            if net_model:
                for key, value in net_model.items():
                    content[f'cost-net-{key}'] = str(value)
        timeframe = content.pop('slice-timeframe', None)
        if timeframe:
            for key, value in timeframe.items():
                content[key] = str(value)
        return content
    except Exception as e:
        logger.error(f'Slice data could not be extracted. {e}')
        return

def extract_dc_part_from_json(content):
    try:
        cost = content.pop('cost')
        part_id = content.pop('dc-slice-part-id')
        content['cost-model'] = cost['model']
        content['cost-value-euros'] = cost['value-euros']
        content['id'] = '{}-{}'.format(part_id['slice-controller-id'],part_id['slice-part-uuid'])
        return content
    except Exception as e:
        logger.error(f'DC Slice Part data could not be extracted. {e}')
        return

def extract_net_part_from_json(content):
    try:
        cost = content.pop('cost')
        part_id = content.pop('net-slice-part-id')
        content['cost-model'] = cost['model']
        content['cost-value-euros'] = cost['value-euros']
        content['id'] = '{}-{}'.format(part_id['slice-controller-id'],part_id['slice-part-uuid'])
        links = content.pop('links')
        for x in links:
            if list(x.keys())[0] == 'requirements':
                requirements = list(x.values())[0]
            elif list(x.keys())[0] == 'dc-part1':
                dc_part_1 = list(x.values())[0]
            elif list(x.keys())[0] == 'dc-part2':
                dc_part_2 = list(x.values())[0]
            else:
                raise Exception('Invalid links section.')
        for key, value in requirements.items():
            if type(value) is int:
                content[f'required-{key}'] = value
            elif type(value) is str:
                content[f'required-{key}'] = check_int(value)
            else:
                content[f'required-{key}'] = str(value)
        dc_part_1 = '{}-{}'.format(dc_part_1['dc-slice-controller-id'], dc_part_1['slice-part-uuid'])
        dc_part_2 = '{}-{}'.format(dc_part_2['dc-slice-controller-id'], dc_part_2['slice-part-uuid'])
        content['link'] = f'{dc_part_1}:{dc_part_2}'
        content = {**content.pop('link-ends'), **content}
        return content
    except Exception as e:
        logger.error(f'DC Slice Part data could not be extracted. {e}')
        return

def set_slice_parameters_for_json(properties):
    properties['slice-constraints'] = {}
    properties['slice-lifecycle'] = {}
    properties['slice-requirements'] = {'reliability': {}}
    properties['slice-cost'] = {'xdc-model': {}, 'xnet-model': {}}
    for key, value in properties.items():
        if type(value) is str:
            properties[key] = check_for_struct(value)
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    properties['slice-timeframe'] = {'service-start-time': properties.pop('service-start-time'), 'service-stop-time': properties.pop('service-stop-time')}
    for key in list(properties.keys()):
        if key.startswith('constraint'):
            properties['slice-constraints'][key[11:]] = properties.pop(key)
        elif key.startswith('lifecycle'):
            properties['slice-lifecycle'][key[10:]] = properties.pop(key)
        elif key.startswith('requirement'):
            properties['slice-requirements'][key[12:]] = properties.pop(key)
        elif key.startswith('cost'):
            properties['slice-cost'][key[5:]] = properties.pop(key)
    for key in list(properties['slice-requirements'].keys()):
        if key.startswith('reliability-'):
            properties['slice-requirements']['reliability'][key[12:]] = properties['slice-requirements'].pop(key)
    for key in list(properties['slice-cost'].keys()):
        if key.startswith('dc'):
            properties['slice-cost']['xdc-model'][key[3:]] = properties['slice-cost'].pop(key)
        elif key.startswith('net'):
            properties['slice-cost']['xnet-model'][key[4:]] = properties['slice-cost'].pop(key)
    properties['cost'] = properties.pop('slice-cost')
    properties['cost']['dc-model'] = properties['cost'].pop('xdc-model')
    properties['cost']['net-model'] = properties['cost'].pop('xnet-model')
    return properties
    
def set_part_parameters_for_json(properties):
    _type = next(iter(properties.keys()))
    properties = properties.pop(_type)
    for key, value in properties.items():
        if type(value) is str:
            properties[key] = check_for_struct(value)
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    slice_controller_id, slice_part_uuid = [int(x) for x in properties.pop('id').split('-')]
    properties[_type + '-id'] = {'slice-controller-id': slice_controller_id, 'slice-part-uuid': slice_part_uuid}
    properties['cost'] = {}
    if _type == 'net-slice-part':
        properties['links'] = [{'dc-part1': {}}, {'dc-part2': {}}, {'requirements': {}}]
        properties['cost'] = {'model': properties.pop('cost-model'), 'value-euros': properties.pop('cost-value-euros')}
        for key in list(properties.keys()):
            if key.startswith('required-'):
                nkey = key[9:]
                if nkey.endswith('gb'):
                    nkey = nkey[:-2] + 'GB'
                properties['links'][2]['requirements'][nkey] = properties.pop(key)
        properties['link-ends'] = {'link-end1-ip': properties.pop('link-end1-ip'), 'link-end2-ip': properties.pop('link-end2-ip')}
    elif _type == 'dc-slice-part':
        properties['cost'] = {'model': properties.pop('cost-model'), 'value-euros': properties.pop('cost-value-euros')}
    return {_type: properties}

def set_controller_parameters_for_json(properties):
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    if properties.get('wan-slice-provider'):
        return {'wan-slice-controller': properties}
    elif properties.get('dc-slice-provider'):
        return {'dc-slice-controller': properties}

def set_wim_parameters_for_json(properties):
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    if properties.get('wim-ref'):
        properties['wim-ref'] = {'ip': properties.pop('ip'), 'port': properties.pop('port')}
    else:
        properties['wim-ref'] = 'undefined'
    return {'WIM': properties}

def set_vim_parameters_for_json(properties):
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    vim_ref = {'vim-ref': {'ip-api': properties.pop('ip-api'), 'port-api': properties.pop('port-api'),
                           'ip-ssh': properties.pop('ip-ssh'), 'port-ssh': properties.pop('port-ssh')}}
    vim_credential = {'vim-credential': {'user-ssh': properties.pop('user-ssh'), 'password-ssh': properties.pop('password-ssh')}}
    properties.update(vim_ref)
    properties.update(vim_credential)
    vswitch = {'vswitch': {'type': properties.pop('vswitch-type'), 'bridge-name': properties.pop('vswitch-name')}}
    properties.update(vswitch)
    return {'VIM': properties}

def set_monitor_parameters_for_json(properties):
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    metrics = properties.pop('metrics')
    metrics = {'metrics': [{'metric': {'name': m}} for m in metrics]}
    properties.update(metrics)
    return {'monitoring-parameters': properties}

def set_vdu_parameters_for_json(properties):
    for key, value in properties.items():
        if type(value) is str:
            properties[key] = check_for_struct(value)
    for key in list(properties.keys()):
        properties[to_underscore(key)] = properties.pop(key)
    op_properties = {'architecture': properties.pop('os-architecture'), 'type': properties.pop('os-type'),
                'distribution': properties.pop('os-distribution'), 'version': properties.pop('os-version')}
    properties.update({'epa-attributes': {'host-epa': {'os-properties': op_properties}}})
    host_epa_properties = ['cpu-architecture', 'cpu-number', 'storage-gb', 'memory-mb', 'host-image']
    for p in host_epa_properties:
        properties['epa-attributes']['host-epa'][p] = properties.pop(p)
    return {'vdu': properties}

def set_elasticity_parameters_for_json(policy_db, actions):
    policy = policy_db['policy']
    vertical_actions = [a['action'] for a in actions if a['action']['type']=='vertical']
    horizontal_actions = [a['action'] for a in actions if a['action']['type']=='horizontal']
    timers = policy.pop('timers')
    timers = timers[1:-1].split(', ')
    timers = {'service-redeployment': timers[0].split(': ')[1], 'reactivate-elasticity': timers[1].split(': ')[1]}
    timers = {'timers': timers}
    for key, value in policy.items():
        if type(value) is str:
            policy[key] = check_for_struct(value)
    for key in list(policy.keys()):
        policy[to_underscore(key)] = policy.pop(key)
    vertical = []
    for v in vertical_actions:
        for key, value in v.items():
            if type(value) is str:
                v[key] = check_for_struct(value)
        vertical.append(v)
    for v in vertical:
        v.pop('type')
    for v in vertical:
        for key in list(v.keys()):
            v[to_underscore(key)] = v.pop(key)
    horizontal = []
    for h in horizontal_actions:
        for key, value in h.items():
            if type(value) is str:
                h[key] = check_for_struct(value)
        horizontal.append(h)
    for h in horizontal_actions:
        h.pop('type')
    for h in horizontal:
        for key in list(h.keys()):
            h[to_underscore(key)] = h.pop(key)
    for h in horizontal:
        if 'priority' in h:
            i = horizontal.index(h)
    horizontal[0], horizontal[i] = horizontal[i], horizontal[0]
    post_deployment = {'post-deployment': timers}
    print(policy)
    print()
    print(vertical)
    print()
    print(horizontal)
    print()
    post_deployment['post-deployment'].update({'actions': {'vertical': [{'action': v} for v in vertical], 'horizontal': [{'action': h} for h in horizontal]}})
    print(post_deployment)
    metric_collector = {'metric-collector': {'node-type': policy.pop('metric-node-type'), 'granularity': policy.pop('metric-granularity'), 'metric-name': policy.pop('metric-name')}}
    deployment = {'deployment': {'constraints': {'vertical': policy.pop('vertical-constraint'), 'horizontal': policy.pop('horizontal-constraint')}}}
    policy.update(metric_collector)
    policy.update(post_deployment)
    policy.update(deployment)
    print()
    print(policy)
    return policy