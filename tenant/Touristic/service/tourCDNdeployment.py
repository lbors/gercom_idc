# extract Addresses
import ast, json, copy, yaml
from functools import *
import pprint, sys, socket
import paramiko , time

#DNS post
dnsPort = 5000
#Core dc public IP
corePublic = "None"
#Edges Public ips
brPublic = grPublic = spPublic = "None"
#Edges SSH ports
corePort = brPort = grPort = spPort = "None"
#Internal private IPs
coreIP = brazilIP = greeceIP = spainIP = "None"
#Edge username and password
edgeUname = "root"
edgePass = "necos"
#Core username and password
coreUname = "necos"
corePass = "necos09"

def main():
    print('Address Extractor')
    print('FileName:' + str(sys.argv[1]))
    try:
        with open(sys.argv[1]) as handle:
            sliceTopology = yaml.safe_load(handle)
    except Exception as e:
        print('Could not load YAML File: {0}'.format(e))
    print(getSlicePartsIps(extract_address(sliceTopology)))
    print("__________________________")
    print(getSSHports(extract_address(sliceTopology)))
    print("__________________________")
    try:
        with open(sys.argv[2]) as dnsCon:
            dnsConf = json.load(dnsCon)
            message=configureDNS(dnsConf)
    except Exception as e:
        print('Could not load DNS json File: {0}'.format(e))
    statCoreServices()
    time.sleep(1)
    print("start DNS server")
    startDNSserver()
    time.sleep(1)
    print("configure dns server")
    sendMessageToDNS(dnsConf)
    time.sleep(1)
    print("start Grafana server")
    startGrafanaServer()
    print("start edges content services")
    startGreekContentServices()
    startSpanishContentServices()
    startBrazilianContentServices()
    print("configure edges dns server")
    cofigResolvConf(grPublic,grPort,edgeUname,edgePass)
    cofigResolvConf(brPublic,brPort,edgeUname,edgePass)
    cofigResolvConf(spPublic,spPort,edgeUname,edgePass)
    print("start load testing tools")
    startEdgeLoadTesting(grPublic,grPort,edgeUname,edgePass)
    startCoreLoadTesting(grPublic,grPort,edgeUname,edgePass)


# Extracts the information from the topology
def extract_address(sliceTopology):
    dc_slice_parts = list(filter( ( lambda x: fetch(x,['dc-slice-part','name']) != None),\
                              fetch(sliceTopology,['Response','Response','Response','slices','sliced','slice-parts']) ))
    # Extracting Infom
    for dc_part in dc_slice_parts:
        print(fetch(dc_part,['dc-slice-part','VIM','vim-credential']))
        print(fetch(dc_part,['dc-slice-part','VIM','vim-ref']))
        print(ipInfo(fetch(dc_part,['dc-slice-part','name']),dc_part))
        print("__________________________")
    #map((lambda x: infoPrint(x)),dc_slice_parts)
#		print fetch(dc_part,['name']
    return dc_slice_parts

def getSlicePartsIps(dc_slice_parts):
    global coreIP, brazilIP, greeceIP, spainIP
    global brPublic, grPublic, spPublic
    for dc_part in dc_slice_parts:
        if fetch(dc_part,['dc-slice-part','name']) == 'core-dc':
            corePublic = fetch(dc_part,['dc-slice-part','VIM','vim-ref','ip-ssh'])
            print("core Public = ",corePublic)
            coreIP = getIPfromVDU(fetch(dc_part,['dc-slice-part','name']),dc_part)
            print("core Internal = ",coreIP)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-brazil':
            brazilIP = getIPfromVDU(fetch(dc_part,['dc-slice-part','name']),dc_part)
            print("Brazil Internal = ",brazilIP)
            brPublic = fetch(dc_part,['dc-slice-part','VIM','vim-ref','ip-ssh'])
            print("Brazilian Public = ",brPublic)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-greece':
            greeceIP = getIPfromVDU(fetch(dc_part,['dc-slice-part','name']),dc_part)
            print("Greece Internal = ",greeceIP)
            grPublic = fetch(dc_part,['dc-slice-part','VIM','vim-ref','ip-ssh'])
            print("Greek Public = ",grPublic)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-spain':
            spainIP = getIPfromVDU(fetch(dc_part,['dc-slice-part','name']),dc_part)
            print("Spain Internal = ",spainIP)
            spPublic = fetch(dc_part,['dc-slice-part','VIM','vim-ref','ip-ssh'])
            print("Spanish Public = ",spPublic)

def getSSHports(dc_slice_parts):
    global corePort, brPort, grPort, spPort
    for dc_part in dc_slice_parts:
        if fetch(dc_part,['dc-slice-part','name']) == 'core-dc':
            corePort = fetch(dc_part,['dc-slice-part','VIM','vim-ref','port-ssh'])
            print("core ssh port = ",corePort)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-brazil':
            brPort = fetch(dc_part,['dc-slice-part','VIM','vim-ref','port-ssh'])
            print("Brazil ssh port = ",brPort)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-greece':
            grPort = fetch(dc_part,['dc-slice-part','VIM','vim-ref','port-ssh'])
            print("Greece ssh port = ",grPort)
        elif fetch(dc_part,['dc-slice-part','name']) == 'edge-dc-slice-spain':
            spPort = fetch(dc_part,['dc-slice-part','VIM','vim-ref','port-ssh'])
            print("Spain ssh port = ",spPort)

# Extracts information from the part that relates to addresses of VDUs
# returns a list of tupples where each tupple has the form
def ipInfo(name, dc_slice_part):
    return [(name, fetch(x,['vdu','id']),fetch(x,['vdu','ip'])) \
                  for x in fetch(dc_slice_part,['dc-slice-part','VIM','vdus']) ]

def getIPfromVDU(name, dc_slice_part):
    return [fetch(x,['vdu','ip']) \
                  for x in fetch(dc_slice_part,['dc-slice-part','VIM','vdus']) ]

def configureDNS(jsonMessage):
    domains=jsonMessage["dnsData"]["dnsEntries"]
    print(jsonMessage["dnsData"]["dnsEntries"])
    for domain in domains:
        if domain["domain"] == "core.swn.uom.gr":
            print("i am in")
            domain["records"]["core"].append(coreIP)
        elif domain["domain"] == "brazil.swn.uom.gr":
            domain["records"]["core"].append(coreIP)
            domain["geoLocation"]["brazil"].append(brazilIP)
            domain["records"]["brazil"].append(brazilIP[1])
        elif domain["domain"] == "greece.swn.uom.gr":
            domain["records"]["core"].append(coreIP)
            domain["geoLocation"]["greece"].append(greeceIP)
            domain["records"]["greece"].append(greeceIP[1])
        elif domain["domain"] == "spain.swn.uom.gr":
            domain["records"]["core"].append(coreIP)
            domain["geoLocation"]["spain"].append(spainIP)
            domain["records"]["spain"].append(spainIP[1])

    print("------------------------")
    print(json.dumps(jsonMessage))
    return jsonMessage

def statCoreServices():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=corePublic,port=corePort, username=coreUname, password=corePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('./start_services')
    ssh.close()

def startDNSserver():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=corePublic,port=corePort, username=coreUname, password=corePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('cd swn-dns && echo necos09 | sudo -S node app-dns.js')
    ssh.close()

def startGrafanaServer():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=corePublic,port=corePort, username=coreUname, password=corePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('echo necos09 | sudo -S service grafana-server start ')
    ssh.close()

def startGreekContentServices():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=grPublic,port=grPort, username=edgeUname, password=edgePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('docker run -p 8081:8081 -p 80:80 swnuom/touristic-services-greece')
    ssh.close()

def startBrazilianContentServices():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=brPublic,port=brPort, username=edgeUname, password=edgePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('docker run -p 8082:8082 -p 80:80 swnuom/touristic-services-spain')
    ssh.close()

def startSpanishContentServices():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=spPublic,port=spPort, username=edgeUname, password=edgePass)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('docker run -p 8080:8080 -p 80:80 swnuom/touristic-services-brazil')
    ssh.close()

def cofigResolvConf(hn,p,uname,password):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=hn,port=p, username=uname, password=password)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('cd.. && echo nameserver {} > /etc/resolv.conf'.format(coreIP))
    ssh.close()

def startEdgeLoadTesting(hn,p,uname,password):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=hn,port=p, username=uname, password=password)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('git clone https://github.com/SWNRG/benchmarking.git && cd benchmarking/jmeter_edge && ./test.sh')
    ssh.close()

def startCoreLoadTesting(hn, p, uname, password):
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=hn,port=p, username=uname, password=password)
    ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command('cd benchmarking/jmeter_core && ./test.sh')
    ssh.close()


def sendMessageToDNS(message):
    TCP_IP = coreIP
    TCP_PORT = dnsPort
    BUFFER_SIZE = 1024
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print(TCP_IP, TCP_PORT)
    s.connect((TCP_IP, TCP_PORT))
    string_message= json.dumps(message)
    s.send(string_message)
    print("MESSAGE TO DNS")
    print(string_message)
    print("----------------")
    data = s.recv(BUFFER_SIZE)
    s.close()
    print("message to DNS",message)
    print("received data:", data)


# fetch a field from a deeply nested dictionary
def fetch(adict,info):
    try:
        info.insert(0,adict)
        return reduce( (lambda x, y: x.get(y)) ,info)
    except:
        return None

# the usual
if __name__ == "__main__":
    main()
