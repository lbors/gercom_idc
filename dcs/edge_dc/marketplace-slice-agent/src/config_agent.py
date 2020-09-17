AGENT_IP_PORT = "10.126.1.187:8001"
BROKER_IP_PORT = "10.126.1.146:8000"
MONGO_IP_PORT = "10.126.1.187:27017"
DB_NAME = "agentdb"
COLLECTION_DC = 'agent_dc_pdts'
COLLECTION_LOCAL_RESOURCES_DC = 'agent_local_resources_dc'
COLLECTION_NET = 'agent_net_pdts'
COLLECTION_LOCAL_RESOURCES_NET = 'agent_local_resources_net'

location = 'AMERICA.BRAZIL'
providerType = 'dc/net'
#DC_TYPE = "DC"
DC_TYPE = "EDGE"


dc_slice_provider = "IDC"
dc_ip = "10.126.1.187"
dc_port = 5000
dc_cost=50
dc_id = 1
dc_EXPTIME = 30


wan_slice_provider = "WAN IDC"
wan_ip = "10.126.1.187"
wan_port = 3030
wan_cost=50
wan_id = 1
wan_EXPTIME = 30

FIRST_LOCAL_RESOURCES_DC = {"_id": dc_id,
    # "model": "CORE2DUO",
    "architecture": "X86_64",
    "vendor": "INTEL",
    "number": 30000000,
    "storage_gb": 295,
    "memory_mb": 1700}

FIRST_LOCAL_RESOURCES_NET = [{"dc-part1": "core-dc",  "dc-part2": "edge-dc-slice-brazil",  "bandwidth-GB": 100000000000},
                        {"dc-part1": "core-dc",  "dc-part2": "edge-dc-slice-greece",  "bandwidth-GB": 100000000000}]
 
