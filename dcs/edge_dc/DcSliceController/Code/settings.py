# paths 
app_path = "/root_path"
log_file= app_path + "/Logs/logs.log"
log_time= app_path + "/Logs/times.log"
datastore_path = '/mnt/Code/network_manager/datastore'
dnsmasq_file = '/mnt/dnsmasq.leases'

# network manager
# change following the model: OVSDB_ADDR = 'tcp:XEN-HOST-IP:6640'
OVSDB_ADDR = 'tcp:10.126.1.187:6640'

# database information
db_user = 'necos'
db_password = 'dcscPass!'
db_host = 'dcsc-mysql' 
db_name = 'dc_slice_controller'

# xen server
xen_host = '10.126.1.187' 
xen_user = 'necos'
xen_port = '22' 

# control interface name
bridge_control = 'br-control'

# kubernetes template (change only if you made your own template)
kube_credencials = {
    "username":"root",
    "password":"necos"
}

# xen template (change only if you made your own template)
xen_credencials = {
    "username":"root",
    "password":"necos"
}

# osm template
osm_credencials = {
    #"username":"root",
    #"password":"5gedge"
    "username":"gercom",
    "password":"gercom"
}

# Server PUBLIC ip and port 
server_public_ip = "10.126.1.187"
server_public_port = "22"
