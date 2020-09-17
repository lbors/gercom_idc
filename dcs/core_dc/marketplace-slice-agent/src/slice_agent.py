import requests, hug
from model.db import Mongo
from itertools import chain
from collections import defaultdict
import config_agent
from datetime import datetime
now = datetime.now() 

# creating connections for communicating with Mongo DB / change database information to external file for future
# configuration (to-do)

providerId = config_agent.AGENT_IP_PORT
database = Mongo(config_agent.MONGO_IP_PORT, config_agent.DB_NAME)

#Include local resources in MongoDB
config_agent.dc_id = 1

if config_agent.providerType.find("dc") > -1:
    localresources_dc = database.read_one({"_id": config_agent.dc_id},config_agent.COLLECTION_LOCAL_RESOURCES_DC)
    if localresources_dc == None:
        print("Inseri dc")
        database.insert(config_agent.FIRST_LOCAL_RESOURCES_DC, config_agent.COLLECTION_LOCAL_RESOURCES_DC)

if config_agent.providerType.find("net") > -1:
    if database.count(config_agent.COLLECTION_LOCAL_RESOURCES_NET) == 0:
        for net_slice_part in config_agent.FIRST_LOCAL_RESOURCES_NET:
            print("Inseri net")
            database.insert(net_slice_part, config_agent.COLLECTION_LOCAL_RESOURCES_NET)

print("Bancos criados")

class SliceAgent:
    def __init__(self):
        # register in the broker
        print("Registering...")
        a = requests.get("http://" + config_agent.BROKER_IP_PORT + "/register_provider?providerId=" + providerId + "&providerType=" + config_agent.providerType + "&location=" + config_agent.location)
        data = a.json()
        print(data)


@hug.get()
def pull_resource_offer(resourceDescriptor: hug.types.json):

    request = defaultdict(list)

    if 'dc-slice-part' in resourceDescriptor[0]:

        print(" RESOURCE DESCRIPTOR")
        print(resourceDescriptor)
        localresources = database.read_one({"_id": config_agent.dc_id}, config_agent.COLLECTION_LOCAL_RESOURCES_DC)
        print("LOCAL RESOURCES")
        print(localresources)
        # Value of Cost Random only for test
        memory_mb = localresources['memory_mb']
        cpu_number = localresources['number']
        #cpu_model = localresources['model']
        required_memory = 0
        required_cpunnumber = 0
        value = {}
        for slice_parts in resourceDescriptor:
            print("***************************** ", slice_parts)
            if slice_parts['dc-slice-part']['type'] == config_agent.DC_TYPE:
                time = slice_parts['dc-slice-part']['slice-time-frame']
                start_time = time['service-start-time']
                end_time = time['service-stop-time']
                for start in start_time:
                    for end in end_time:
                        slice_parts['dc-slice-part']['cost']['value-euros'] = config_agent.dc_cost
                        slice_parts['dc-slice-part']['dc-slice-controller']['dc-slice-provider'] = config_agent.dc_slice_provider
                        slice_parts['dc-slice-part']['dc-slice-controller']['ip'] = config_agent.dc_ip
                        slice_parts['dc-slice-part']['dc-slice-controller']['port'] = config_agent.dc_port
                        for vdus in slice_parts['dc-slice-part']['VIM']['vdus']:
                            vdu = vdus['vdu']['epa-attributes']['host-epa']
                            # Slices Resources
                            #required_memory = vdu['memory-mb']['greater_or_equal']
                            if isinstance(vdu['memory-mb'], dict) and 'greater_or_equal' in vdu['memory-mb']:
                                required_memory = vdu['memory-mb']['greater_or_equal']
                            else: 
                                required_memory = vdu['memory-mb']

                            required_cpuarchitecture = vdu['cpu-architecture']
                            required_cpuarchitecture = required_cpuarchitecture.replace("PREFER_", "")
                            required_cpunnumber = vdu['cpu-number']
                            required_storagegb = vdu['storage-gb']
                            #required_os = vdu['os-properties']['architecture']['equal']
                            if isinstance(vdu['os-properties']['architecture'], dict) and  'equal' in vdu['os-properties']['architecture']:
                                required_os = vdu['os-properties']['architecture']['equal']
                            else: 
                                required_os = vdu['os-properties']['architecture']

                            memory_allocated_mb = 0
                            result_cpu = 0

                            now_time = (int(str(now.hour) + str(now.minute) + str(now.second)))
                            firt_cache = database.read_many({'$and': [{'$or': [{'start_service': {'$lte': start}}, {'stop_service': {'$lte': end}}]}, {'$or': [{'start_service': {'$gte': start}}, {'stop_service': {'$gte': end}}]}, {'$or': [{'pre_allocation_time': {'$lte': now_time}}]}]}, config_agent.COLLECTION_DC)
                            if firt_cache != None:
                                database.insert({
                                    "pre_allocation_time": (int(str(now.hour) + str(now.minute) + str(now.second + config_agent.dc_EXPTIME))),
                                    "start_service": start,
                                    "stop_service": end,
                                    "architecture": required_cpuarchitecture,
                                    "number": required_cpunnumber,
                                    "storage_gb": required_storagegb,
                                    "memory_mb": required_memory
                                }, config_agent.COLLECTION_DC)
                                vdus['vdu']['epa-attributes']['host-epa']['memory-mb'] = required_memory
                                vdus['vdu']['epa-attributes']['host-epa']['cpu-architecture'] = required_cpuarchitecture
                                vdus['vdu']['epa-attributes']['host-epa']['cpu-number'] = required_cpunnumber
                                vdus['vdu']['epa-attributes']['host-epa']['storage-gb'] = required_storagegb
                                vdus['vdu']['epa-attributes']['host-epa']['os-properties']['architecture'] = required_os

                            cache = database.read_many({'$and': [{'$or': [{'start_service': {'$lte': start}}, {'stop_service': {'$lte': end}}]}, {'$or': [{'start_service': {'$gte': start}}, {'stop_service': {'$gte': end}}]}]}, config_agent.COLLECTION_DC)
                            for i in cache:
                                mb = i['memory_mb']
                                memory_allocated_mb += mb
                                cpu = i['number']
                                result_cpu += cpu

                            if (memory_allocated_mb == 0) and (result_cpu == 0):
                                print("Cache Empty. Ready for allocate the first resource")
                            else:
                                print("Do have Something")

                            if (memory_allocated_mb + required_memory <= memory_mb) and (
                                    result_cpu + required_cpunnumber <= cpu_number):
                                print(memory_allocated_mb)
                                print(result_cpu)
                                print(memory_mb)
                                print(cpu_number)
                                print("Can Allocate Resource... Allocate Resource")
                                print("Before")
                                print(vdus)
                                database.insert({
                                    "pre_allocation_time": (int(
                                        str(now.hour) + str(now.minute) + str(now.second + config_agent.dc_EXPTIME))),
                                    "start_service": start,
                                    "stop_service": end,
                                    "architecture": required_cpuarchitecture,
                                    "number": required_cpunnumber,
                                    "storage_gb": required_storagegb,
                                    "memory_mb": required_memory
                                }, config_agent.COLLECTION_DC)
                                vdus['vdu']['epa-attributes']['host-epa']['memory-mb'] = required_memory
                                vdus['vdu']['epa-attributes']['host-epa']['cpu-architecture'] = required_cpuarchitecture
                                vdus['vdu']['epa-attributes']['host-epa']['cpu-number'] = required_cpunnumber
                                vdus['vdu']['epa-attributes']['host-epa']['storage-gb'] = required_storagegb
                                vdus['vdu']['epa-attributes']['host-epa']['os-properties']['architecture'] = required_os
                                print("After")
                                print(vdus)
                            else:
                                print("I Can not Allocate resource in this time")                            
                        request["dc-slice-part"].append(slice_parts['dc-slice-part'])

    #

    if 'net-slice-part' in resourceDescriptor[0]:
        localresources = database.read(config_agent.COLLECTION_LOCAL_RESOURCES_NET)

        print("NET RESOURCE DESCRIPTOR")
        print(resourceDescriptor)
        print("LOCAL RESOURCES")
        print(localresources)


        value = {}

        for slice_parts in resourceDescriptor:
            #to-do: Needs to query the local resources for the specific net-slice-part
            bandwidth_total_gb = 100000000000

            time = slice_parts['net-slice-part']['slice-time-frame']
            start_time = time['service-start-time']
            end_time = time['service-stop-time']
            for start in start_time:
                for end in end_time:
                    slice_parts['net-slice-part']['cost']['value-euros'] = config_agent.wan_cost
                    slice_parts['net-slice-part']['wan-slice-controller']['wan-slice-provider'] = config_agent.wan_slice_provider
                    slice_parts['net-slice-part']['wan-slice-controller']['ip'] = config_agent.wan_ip
                    slice_parts['net-slice-part']['wan-slice-controller']['port'] = config_agent.wan_port

                    # Slices Resources

                    print(slice_parts['net-slice-part']['links'])
                    # [{'dc-part1': 'dc-slice1'}, {'dc-part2': 'dc-slice2'}, {'requirements': {'bandwidth-GB': 1}}]
                    bandwidth_required_gb = slice_parts['net-slice-part']['links'][2]['requirements']['bandwidth-GB']
                    print(bandwidth_required_gb)
                    bandwidth_allocated_gb = 0
                    #cache = database.read_many({'$and': [{'start_service': {'$lte': start}}, {'stop_service': {'$gte': end}}, {'pre_allocation_time': {'$gte': end}}]}, config_agent.COLLECTION_RESERVED_WAN)
                    
                    #TO-DO: needs to read the resources allocated for a specific net-slice-part
                    now_time = (int(str(now.hour) + str(now.minute) + str(now.second)))
                    firt_cache = database.read_many({'$and': [{'$or': [{'start_service': {'$lte': start}}, {'stop_service': {'$lte': end}}]}, {'$or': [{'start_service': {'$gte': start}}, {'stop_service': {'$gte': end}}]}, {'$or': [{'pre_allocation_time': {'$lte': now_time}}]}]}, config_agent.COLLECTION_NET)

                    if firt_cache != None:
                        a = database.insert({
                            "pre_allocation_time": (
                                int(str(now.hour) + str(now.minute) + str((now.second) + config_agent.dc_EXPTIME))),
                            "start_service": start,
                            "stop_service": end,
                            "dc-part1": slice_parts['net-slice-part']['links'][0]['dc-part1'],
                            "dc-part2": slice_parts['net-slice-part']['links'][1]['dc-part2'],
                            "bandwidth-GB": bandwidth_required_gb
                        }, config_agent.COLLECTION_NET)


                    cache = database.read_many({'$and': [{'$or': [{'start_service': {'$lte': start}}, {'stop_service': {'$lte': end}}]}, {'$or': [{'start_service': {'$gte': start}}, {'stop_service': {'$gte': end}}]}]},config_agent.COLLECTION_NET)
                    for i in cache:
                        temp = i['bandwidth-GB']
                        bandwidth_allocated_gb += temp

                    if bandwidth_allocated_gb == 0:
                        print("Cache Empty. Ready for allocate the first resource")

                    else:
                        print("Do have Something")

                    if bandwidth_allocated_gb + bandwidth_required_gb <= bandwidth_total_gb:
                        a = database.insert({
                            "pre_allocation_time": (int(str(now.hour) + str(now.minute) + str((now.second) + config_agent.dc_EXPTIME))),
                            "start_service": start,
                            "stop_service": end,
                            "dc-part1": slice_parts['net-slice-part']['links'][0]['dc-part1'],
                            "dc-part2": slice_parts['net-slice-part']['links'][1]['dc-part2'],
                            "bandwidth-GB": bandwidth_required_gb
                        }, config_agent.COLLECTION_NET)
                    else:
                        print("I Can not Allocate resource in this time")
                    request["net-slice-part"].append(slice_parts['net-slice-part'])                        
    
    print("Response to Broker:")
    print(request)
    return request

sa = SliceAgent()