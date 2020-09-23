from flask import Flask, Blueprint, request
from ruamel.yaml import YAML
import yaml, json, requests, re, _thread, time
from logs import logs
from multiprocessing.pool import ThreadPool
from settings import SLICE_BROKER, SRO_IP

pool = ThreadPool(processes=100)
slice_builder = Blueprint('slice_builder', 'slice_builder', url_prefix='/slice_builder')

@slice_builder.route('initiate_slice_creation', methods=['POST'])
def initiate_slice_creation():
    logs.logger.info("Iniciando criação de slice no slice_builder")
    start = time.time()
#     # validate yaml
    try:
        slice_description = request.data
        logs.logger.info(yaml.load(request.data))
    except Exception as e:
        logs.logger.info("Erro na validação do json no slice_builder")
        errors = []
        errors.append("Invalid yaml file!")
        if(len(e.args)>2): errors.append(e.args)
        else: errors.append(e.args)
        logs.logger.info("Erro ao criar slice no slice_builder")
        return logs.callback(0, errors), 400

#    file = open('./Yaml/iot_slice-builder-to-slice-broker.yaml', 'r') 
 #   file_yaml = yaml.load(yaml.load(yaml.dump(file.read())))
# slice_broker = requests.post('http://'+SLICE_BROKER+'/slice_broker', data=file_yaml)
    logs.logger.info("Procurando por slice_parts no slice_builder")

    yaml_response = "0"
    logs.logger.info("Searching slice part alternatives in Marketplace...")
    response = requests.post('http://'+SLICE_BROKER+'/locate_slice_resources', files={'pdt':request.data})
    yaml_response = yaml.load(response._content)
    logs.logger.info(yaml_response)
   # logs.logger.info(yaml_response)
   # file = open('./broker-to-builder.yaml', 'r') 
   # yaml_response = yaml.load(yaml.dump(file.read()))
   # slice_options = yaml.load(yaml.load(yaml.dump((yaml_response))))["slice-parts-options"]
    logs.logger.info("Selecting slice part alternatives based on cost...")

#    YAML_FILE = open('./Yaml/response_broker.yaml', 'r')
#    yaml_response = yaml.load(YAML_FILE.read())
#    logs.logger.info(yaml_response)
#    slice_options = yaml_response["slice-parts-options"]
    if(yaml_response):
        slice_options = yaml.load(yaml_response)["slice-parts-options"]
        #selecting the slices
        id = yaml.load(request.data)["slices"]["sliced"]["id"]
        id_slice = id
        created = id = yaml.load(request.data)["slices"]["sliced"]["created-slice"]
#        file_sro = open('./Yaml/iot_slice-builder-to-slice-resource-orchestrator.yaml', 'r') 
        file_sro = yaml.load(request.data)
        slices = choosing_slices(yaml.dump(slice_options))
        n = 0
        #generating archive for sro
        slices_to_sro = []
        dc_slices_id = []
        #request dc-slices
        dc_slices = slices[0]
        threads = []
        logs.logger.info("Contacting selected resource providers...")
        for row in dc_slices:
            result = send_dc_slice(row, threads)
            if(result == 0):
                return logs.callback(0, "Could not create slice."), 500
        """for thread in threads:
            url = response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["URL"]))
            if (re.search('unicamp', url, re.IGNORECASE)):
                provider = "UNICAMP"
            else: 
                if(re.search('143.106.11.131', url, re.IGNORECASE)):
                    provider = "5TONIC"
                else:
                    if(re.search('195.251.209.100', url, re.IGNORECASE)):
                        provider = "UOM"
                    else:
                        provider = "UFSCAR"
        """
        for thread in threads:
            url = response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["URL"]))
            if (re.search('10.126.1.32', url, re.IGNORECASE)):
                provider = "GERCOM"
            else:
                provider = "IDC"
            response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["ID"]))
            logs.logger.info("Response from "+provider+" DC slice controller: "+ response)
            dc_slices_id.append(thread.get())
        for row in slices[0]:
            slice = row["Slice"]["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]
            vdus = []
            for vdu in slice["VIM"]["vdus"]:
                new_vdu = {"vdu": "null"}
                new_vdu["vdu"] = vdu
                vdus.append(new_vdu)
            slice["VIM"]["vdus"] = vdus
            slice["dc-slice-controller"] = row["Controller"]
            new_slice = {"dc-slice-part": "null"}
            new_slice["dc-slice-part"] = slice
            slices_to_sro.append(new_slice)
        
        ## wan
        #populate wan file
        net_slices_id = []
        try:
            wan_slices = populate_wan_file(slices[1], dc_slices_id, yaml.load(request.data), created)
        except Exception as e:
            print(e)
            return logs.callback(0, "Could not fill request_wan file, check request_dc's response")
        
       # logs.logger.info(yaml.dump(slices_to_sro))
        # request wan
        for row in wan_slices:
            result = send_net_slice(row, net_slices_id)
            if(result == 0):
                return logs.callback(0, "Could not create slice."), 500
        end_r = time.time()
        for row in wan_slices:
            slice = row["Slice"]["slices"]["sliced"]["slice-part"]["net-slice-part"]
            slice["wan-slice-controller"] = row["Controller"]
            dc1 = {"dc-part1": slice["links"]["dc-part1"]}
            dc2 = {"dc-part2": slice["links"]["dc-part2"]}
            req = {"requirements": slice["links"]["requirements"]}

            links = []
            links.append(dc1)
            links.append(dc2)
            links.append(req)
            slice["links"] = links
            new_slice = {"net-slice-part": "null"}
            new_slice["net-slice-part"] = slice
            slices_to_sro.append(new_slice)
        n=0
        for row in slices[2]:
            try:
                slices_to_sro[n]["dc-slice-part"]["cost"] = row
            except:
                slices_to_sro[n]["net-slice-part"]["cost"] = row
            n=n+1
            
        file_sro["slices"]["sliced"]["slice-parts"] = slices_to_sro
        #inserting id of each dc slice part in the file that will be sent to sro
        slices_sro = yaml.load(yaml.dump(file_sro))["slices"]["sliced"]["slice-parts"]
        n = 0
        for slice in slices_sro:
            try:
                dc_slice_part_id = {"slice-controller-id": "null", "slice-part-uuid": "null"}
                dc_slice_part_id["slice-controller-id"] = yaml.load(yaml.load(yaml.dump(dc_slices_id[n]["ID"])))["Response"]["slice-part-id"]["dc-slice-controller-id"]
                dc_slice_part_id["slice-part-uuid"] = yaml.load(yaml.load(yaml.dump(dc_slices_id[n]["ID"])))["Response"]["slice-part-id"]["id"]
                slice["dc-slice-part"]["dc-slice-part-id"] = dc_slice_part_id
                id = str(dc_slice_part_id["slice-controller-id"])+"-"+str(dc_slice_part_id["slice-part-uuid"])      
                n = n+1
                for vdu in slice["dc-slice-part"]["VIM"]["vdus"]:
                    vdu["vdu"]["name"]=vdu["vdu"]["name"]+"-"+id
                    vdu["vdu"]["id"]=vdu["vdu"]["name"]
            except Exception as e:
                print(e)
            
        ##activate dc-slice
        #populate file
        try:
            files_activate = populate_dc_activate_file(id, dc_slices_id)
        except:
            return logs.callback(0, "Could not fill activate_dc file, check request_dc's response")
        try:
            files_activate_wan = populate_wan_activate_file(id, net_slices_id)
        except:
            return logs.callback(0, "Could not fill activate_wan file, check request_wan's response")
        #inserting id of each net slice part in the file that will be sent to sro
        n = 0
        for slice in slices_sro:
            try:
                net_slice_part_id = {"slice-controller-id": "null", "slice-part-uuid": "null"}
                net_slice_part_id["slice-controller-id"] = yaml.load(yaml.load(yaml.dump(net_slices_id[n]["ID"])))["response"]["slice-part-id"]["slice-controller-id"]
                net_slice_part_id["slice-part-uuid"] = yaml.load(yaml.load(yaml.dump(net_slices_id[n]["ID"])))["response"]["slice-part-id"]["uuid"]
                slice["net-slice-part"]["net-slice-part-id"] = net_slice_part_id
                
                n = n+1
            except Exception as e:
                print(e)
                
        ids_activate = []
        # activate
        threads = []
        for row in files_activate:
            try:
            #saving the url, and the slice description
                url = row["URL"]
                slice =  row["Slice"]       
                response = exec_request(url, "/activate",slice, threads)
                if(response!=1):
                    logs.logger.error("Error: unable to start thread")
                    return 0

            except:
                logs.logger.error("Error")
        for thread in threads:
            url = response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["URL"]))
            """if (re.search('unicamp', url, re.IGNORECASE)):
                provider = "UNICAMP"
            else: 
                if(re.search('143.106.11.131', url, re.IGNORECASE)):
                    provider = "5TONIC"
                else:
                    if(re.search('195.251.209.100', url, re.IGNORECASE)):
                        provider = "UOM"
                    else:
                        provider = "UFSCAR"
            response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["ID"]))
            logs.logger.info("Response from "+provider+" DC slice controller: "+ response)
            ids_activate.append(response)"""
        for thread in threads:
            url = response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["URL"]))
            if (re.search('10.126.1.32', url, re.IGNORECASE)):
                provider = "GERCOM"
            else:
                provider = "IDC"
            response = yaml.dump(yaml.load((yaml.load(yaml.dump(thread.get())))["ID"]))
            logs.logger.info("Response from "+provider+" DC slice controller: "+ response)
            ids_activate.append(response)
        ##activate net-slice
        #populate file
        
        # activate
        for row in files_activate_wan:
            try:
            #saving the url, and the slice description
                url = row["URL"]
                slice =  row["Slice"]
                dc_slice_2 = row["dc_slice_2"]    
                try:
                    #logs.logger.info("Activating NET slice part from UNICAMP-"+dc_slice_2+" WAN slice controller")
                    logs.logger.info("Activating NET slice part from GERCOM-"+dc_slice_2+" WAN slice controller")
                    act = post_slice(url, "/activate_slice_part", yaml.dump(slice))
                    response = yaml.dump(yaml.load((yaml.load(yaml.dump(act)))["ID"]))
                    #logs.logger.info("Response from UNICAMP-"+dc_slice_2+" WAN slice controller: "+response)
                    logs.logger.info("Response from GERCOM-"+dc_slice_2+" WAN slice controller: "+response)
                    ids_activate.append(response)
                except:
                    pass

            except:
                logs.logger.error("Error")
        n = 0
        #ids_activate = ["Response:\n  monitoring-handle:\n    ip: 143.106.11.131\n    port: 20272\n  slice-part-id:\n    dc-slice-controller-id: '2'\n    id: 72\n  ssh-handle:\n    ip: 143.106.11.131\n    port: 22272\n  vim-handle:\n    ip: 143.106.11.131\n    port: 21272\nStatus: OK\n", "Response:\n  monitoring-handle:\n    ip: 10.1.0.3\n    port: 19558\n  slice-part-id:\n    dc-slice-controller-id: '5'\n    id: 58\n  ssh-handle:\n    ip: 10.1.0.3\n    port: 22558\n  vim-handle:\n    ip: 10.1.0.3\n    port: 21558\nStatus: OK\n", "message: agents connection error\nresponse:\n  slice-part-id:\n    slice-controller-id: 10\n    uuid: '49'\nstatus: ERROR\n"]
        for slice in slices_sro:
            try:
                row = yaml.load(yaml.load(yaml.dump(ids_activate[n])))["Response"]
                vim_ref = {"ip-api": None, "port-api": None, "ip-ssh":row["ssh-handle"]["ip"], "port-ssh":row["ssh-handle"]["port"]}
                slice["dc-slice-part"]["monitoring-parameters"]["measurements-db-ip"] = row["monitoring-handle"]["ip"]
                slice["dc-slice-part"]["monitoring-parameters"]["measurements-db-port"] = row["monitoring-handle"]["port"]
                if(row["vim-handle"]!= "None"):
                    vim_ref = {"ip-api": row["vim-handle"]["ip"], "port-api":row["vim-handle"]["port"], "ip-ssh":row["ssh-handle"]["ip"], "port-ssh":row["ssh-handle"]["port"]}
                slice["dc-slice-part"]["VIM"]["vim-ref"] = vim_ref
                #vswitch = {"type": "openvswitch", "bridge-name": "br_2_1"}
                #slice["dc-slice-part"]["VIM"]["vswitch"] = vswitch
                slice["dc-slice-part"]["VIM"]["vim-credential"] = row["vim-credential"]
                slice["dc-slice-part"]["VIM"]["vswitch"] = row["vswitch"]
               # logs.logger.info(yaml.dump(slice))
                
                n = n+1
            except Exception as e:
                #logs.logger.error(e)
                print(e)
#        logs.logger.info(ids_activate)
        file_sro["slices"]["sliced"]["slice-parts"] = slices_sro
       # logs.logger.info(yaml.dump(file_sro))
        status = ""       
        try:
            if(str(created) == 'False'):
     #       logs.logger.info(yaml.dump(file_sro))
                status = "created"
                sro_response = requests.post('http://'+SRO_IP+':5005/necos/sro/slices/req_e2e_binding', data=yaml.dump(file_sro))
                logs.logger.info("Response from SRO: "+str(sro_response._content))
            else:
                status = "updated"
        except Exception as e:
            logs.logger.error(e)
            logs.logger.error("Could not connect to SRO")
        end = time.time()
        logs.echo_to_file("---------------------------------------------")
        logs.echo_to_file("Time slice reservation: " + str(end_r - start) + " seconds.")
        logs.echo_to_file("Slice ID = "+id_slice+" "+status+" in " + str(end - start) +" seconds.")
        return logs.callback(1, file_sro), 201
    else:
        print("Erro ao comunicar com Broker")
        return logs.callback(0, "ERROR"), 404
    
def choosing_slices (slice_options):
    dc_slices = []
    net_slices = []
    slices_selected = []
    costs = []
    options = yaml.load(slice_options)
    for option in options:
#        logs.logger.info(yaml.dump(option))
        try:
            #type: DC
            name = option["dc-slice-choices"]["name"]
            
            #logs.logger.info(name)
            slice_choices = yaml.load(yaml.dump(option["dc-slice-choices"]["alternatives"]))
            #logs.logger.info(slice_choices)
            best_alternative = slice_choices[0]["dc-slice-part"]
            cost = yaml.load(yaml.dump(best_alternative["cost"]))
            best_cost = yaml.load(yaml.dump(best_alternative["cost"]))["value-euros"]
            dc_slice_controller = best_alternative["dc-slice-controller"]
            #logs.logger.info(yaml.dump(first_alternative))
            # #selecting the best cost alternative
            for row in slice_choices:
                alternative = row["dc-slice-part"]
                if(yaml.load(yaml.dump(alternative["cost"]))["value-euros"] < best_cost):
                    cost = yaml.load(yaml.dump(alternative["cost"]))
                    best_cost = yaml.load(yaml.dump(alternative["cost"]))["value-euros"]
                    dc_slice_controller = alternative["dc-slice-controller"]
                    best_alternative = alternative
                    
            # #search the file of controller 
            controller = select_controller(dc_slice_controller)
            
            #logs.logger.info(controller)
            # #creating a dictionary containing the controller and the slice description
            slice = {"Controller": controller[0], "Slice": "null"}
            ##########################
            file_yaml = populate_dc_file(yaml.load(request.data), controller[0], controller[1], name, alternative)
            #inserting the updated description of each slice into the dictionary
            slice["Slice"] = file_yaml
            #logs.logger.info(yaml.dump(slice))
            #adds to the array of slices
            costs.append(cost)
            dc_slices.append(slice)
  #          logs.logger.info("ok")
        except Exception as e:
           # print(e)
    #         #type: WAN
            name = option["net-slice-choices"]["name"]
            slice_choices = yaml.load(yaml.dump(option["net-slice-choices"]["alternatives"]))
            #selecting the best cost alternative
            first_alternative = slice_choices[0]["net-slice-part"]
            cost = yaml.load(yaml.dump(first_alternative["cost"]))
            best_cost = yaml.load(yaml.dump(first_alternative["cost"]))["value-euros"]
            wan_slice_controller = first_alternative["wan-slice-controller"]
            for row in slice_choices:
                alternative = row["net-slice-part"]
                if(yaml.load(yaml.dump(alternative["cost"]))["value-euros"] < best_cost):
                    cost = yaml.load(yaml.dump(alternative["cost"]))
                    best_cost = yaml.load(yaml.dump(alternative["cost"]))["value-euros"]
                    wan_slice_controller = alternative["wan-slice-controller"]
            #controller = {"controller-id": '9', "ip": 'necos.dca.fee.unicamp.br', "port": '4000'}
            controller = {"controller-id": '1', "ip": '10.126.1.32', "port": '4000'}
            slice = {"Controller": controller, "Slice": "null"}
            try:
             
                #inserting the slice description in the controller file
                file = open('./Yaml/iot_slice-builder-to-wan-slice-controller.yaml', 'r')
                file_yaml = yaml.load(file.read())
                file_yaml["slices"]["sliced"]["slice-part"]["net-slice-part"]["name"] = name
                             
   #logs.logger.info(yaml.dump(file_yaml))
            except Exception as e:
                logs.logger.info(e.args)
                errors = []
                errors.append("Invalid yaml file!")
                if(len(e.args)>2): errors.append(e.args[2])
                else: errors.append(e.args)
                logs.callback(0, errors)
                return "0", 400
            
            #inserting the updated description of each slice into the dictionary
            slice["Slice"] = file_yaml
            #adds to the array of slices
            costs.append(cost)
            net_slices.append(slice)
    slices_selected.append(dc_slices)
    slices_selected.append(net_slices)
    slices_selected.append(costs)
    
    return slices_selected

def post_slice(url, req_act, slice):
#    logs.logger.info(slice)
#    logs.logger.info(url)
    slice_id = requests.post(url+req_act, data=slice)
    response = {"URL": url, "ID": slice_id._content}
    return response
    
def exec_request(url, req_act, slice, threads):
    slice = yaml.dump(slice)
    #initiating a thread for each request
    try:
        if (re.search('request', req_act, re.IGNORECASE)):
            """if(re.search('unicamp', url, re.IGNORECASE)):
                logs.logger.info("Resquesting from UNICAMP DC slice controller...")
            else:
                if(re.search('143.106.11.131', url, re.IGNORECASE)):
                    logs.logger.info("Resquesting from 5TONIC DC slice controller...")
                else:
                    if(re.search('195.251.209.100', url, re.IGNORECASE)):
                        logs.logger.info("Resquesting from UOM DC slice controller...")
                    else:
                        logs.logger.info("Resquesting from UFSCAR DC slice controller...")"""

            if (re.search('10.126.1.32', url, re.IGNORECASE)):
                logs.logger.info("Resquesting from GERCOM DC slice controller...")
            else:
                logs.logger.info("Resquesting from IDC DC slice controller...")
        else:
            """if(re.search('unicamp', url, re.IGNORECASE)):
                logs.logger.info("Activating from UNICAMP DC slice controller...")
            else:
                if(re.search('143.106.11.131', url, re.IGNORECASE)):
                    logs.logger.info("Activating from 5TONIC DC slice controller...")
                else:
                    if(re.search('195.251.209.100', url, re.IGNORECASE)):
                        logs.logger.info("Activating from UOM DC slice controller...")
                    else:
                        logs.logger.info("Activating from UFSCAR DC slice controller...")"""
            if (re.search('10.126.1.32', url, re.IGNORECASE)):
                logs.logger.info("Activating from GERCOM DC slice controller...")
            else:
                logs.logger.info("Activating from IDC DC slice controller...")
        async_call = pool.apply_async(post_slice, (url, req_act, slice))
        threads.append(async_call)
        return 1
    except Exception as e:
        return e.args

def send_dc_slice(row, threads):
    try:
        #saving the controller, and the slice description
        controller = row["Controller"]
        slice =  row["Slice"] 
        #ip and controller port
        ip = controller["ip"]  
        port = controller["port"]
        #mounting the request url
        url = 'http://'+str(ip)+':'+str(port)+'/slice_part'
        #url = 'http://localhost:5005/slice_controller'
        #slice type
        
        try: 
            type = slice["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["type"]
        except:
            type = "NET"
        try:
            #adds slice_id to array of ids
            #logs.logger.info(yaml.dump(slice))
            if(type!="NET"): 
                response = exec_request(url, "/request",slice, threads)
                if(response!=1):
                    logs.logger.error("Error: unable to start thread")
                    return 0
        except:
            return 0

    except:
        logs.logger.error("Controller does not exist")
        return 0
    return 1

def send_net_slice(row, slices_id):

    try:
        #saving the controller, and the slice description
        controller = row["Controller"]
        slice =  row["Slice"]       
        id_dc_controller = str(slice["slices"]["sliced"]["slice-part"]["net-slice-part"]["links"]['dc-part2']['dc-slice-controller-id'])
        """if(id_dc_controller == "6"):
            dc_controller = "UOM"
        else:
            if(id_dc_controller == "5"):
                dc_controller = "5TONIC"
            else:    
                dc_controller = "UFSCAR" """
        # Baseado no arquivo do wan master
        if(id_dc_controller == "2"):
            dc_controller = "GERCOM"
        else:
            dc_controller = "IDC"        
#ip and controller port
        ip = controller["ip"]
        port = controller["port"]
        #mounting the request url
        url = 'http://'+str(ip)+':'+str(port)
        #url = 'http://localhost:5005/slice_controller'
        #slice type
        try:
            #logs.logger.info("Requesting from UNICAMP-"+dc_controller+" WAN slice controller...")
            logs.logger.info("Requesting from GERCOM-"+dc_controller+" WAN slice controller...")
            req = post_slice(url, "/request_slice_part",yaml.dump(slice))
            req["dc_slice_2"] = dc_controller
            response = yaml.dump(yaml.load((yaml.load(yaml.dump(req)))["ID"]))
            #logs.logger.info("Response from UNICAMP-"+dc_controller+" WAN slice controller: "+ response)
            logs.logger.info("Response from GERCOM-"+dc_controller+" WAN slice controller: "+ response)
            slices_id.append(req)
        except:
            return 0
    except:
        logs.logger.error("Controller does not exist")
        return 0
    return 1

def populate_wan_file(net_slices, dc_slices_id, request_file, created):
    links =[]
    first_id = ""     
    size = len(dc_slices_id)
    if(str(created) == 'True'):
        controller_id = request_file["slices"]["sliced"]["slice-parts"][1]["net-slice-part"]["links"][0]["dc-part1"]["id"]["controller-id"]
        slice_id = request_file["slices"]["sliced"]["slice-parts"][1]["net-slice-part"]["links"][0]["dc-part1"]["id"]["slice-part-id"]
        first_id = {"slice-part-id": {"dc-slice-controller-id": controller_id, "id": slice_id}}
    else:
        if(str(created) == 'False'):
            for x in range (0, size):
            #    logs.logger.info(dc_slices_id[x])
                id = dc_slices_id[x]["ID"]
                id = yaml.load(yaml.load(yaml.dump(id)))["Response"]
                if(id["slice-part-id"]["dc-slice-controller-id"]=='2'):
                    first_id = id
    #grouping ids
    for x in range(0, size):
        id = dc_slices_id[x]
        last_id = yaml.load(yaml.dump(id))["ID"]
        last_id = yaml.load(yaml.load(yaml.dump(last_id)))["Response"]
        if(first_id!=last_id):
            link = {"dc-slice1": first_id, "dc-slice2": last_id}
            print(link)
            links.append(link)
    
    n = 0

#    logs.logger.info(links)
    for row in net_slices:
        dc_slice1 = {"dc-slice-controller-id": links[n]["dc-slice1"]["slice-part-id"]["dc-slice-controller-id"], 'slice-part-uuid': links[n]["dc-slice1"]["slice-part-id"]["id"]}
        row["Slice"]["slices"]["sliced"]["slice-part"]["net-slice-part"]["links"]["dc-part1"] = dc_slice1
        dc_slice2 = {'dc-slice-controller-id': links[n]["dc-slice2"]["slice-part-id"]["dc-slice-controller-id"], 'slice-part-uuid': links[n]["dc-slice2"]["slice-part-id"]["id"]}
        row["Slice"]["slices"]["sliced"]["slice-part"]["net-slice-part"]["links"]["dc-part2"] = dc_slice2
        
        row["Slice"]["slices"]["sliced"]["id"] = request_file["slices"]["sliced"]["id"]
        row["Slice"]["slices"]["sliced"]["name"] = request_file["slices"]["sliced"]["name"]
        row["Slice"]["slices"]["sliced"]["short-name"] = request_file["slices"]["sliced"]["short-name"]
        row["Slice"]["slices"]["sliced"]["description"] = request_file["slices"]["sliced"]["description"]
        row["Slice"]["slices"]["sliced"]["slice-requirements"] = request_file["slices"]["sliced"]["slice-requirements"]
        row["Slice"]["slices"]["sliced"]["slice-lifecycle"] = request_file["slices"]["sliced"]["slice-lifecycle"]
        row["Slice"]["slices"]["sliced"]["slice-timeframe"] = request_file["slices"]["sliced"]["slice-timeframe"]
        row["Slice"]["slices"]["sliced"]["vendor"] = request_file["slices"]["sliced"]["vendor"]   
        n = n+1
        
    return net_slices

def populate_dc_activate_file(id_slice, slices_id):
    files = []
    for row in slices_id:
        file = open('./Yaml/iot_slice-builder-to-dc-slice-controller-activate-slice-unicamp.yaml', 'r')
        file_yaml = yaml.load(file.read())
    
        url = yaml.load(yaml.dump(row))["URL"]
        id = yaml.load(yaml.dump(row))["ID"]
        id = yaml.load(id)["Response"]
        file_yaml["slices"]["sliced"]["slice-id"] = id_slice
        file_yaml["slices"]["sliced"]["slice-part-id"]["slice-controller-id"] = id["slice-part-id"]["dc-slice-controller-id"]
        file_yaml["slices"]["sliced"]["slice-part-id"]["uuid"] = id["slice-part-id"]["id"]
        tuple = {"URL": url, "Slice": file_yaml}
        files.append(tuple)
    ##return files and urls
    return files

def populate_wan_activate_file(id_slice, slices_id):
    files = []
    for row in slices_id:
        file = open('./Yaml/iot_slice-builder-to-wan-slice-controller-activate-slice.yaml', 'r')
        file_yaml = yaml.load(file.read())
    
        url = yaml.load(yaml.dump(row))["URL"]
        dc_slice_2 = yaml.load(yaml.dump(row))["dc_slice_2"]
        id = yaml.load(yaml.dump(row))["ID"]
        id = yaml.load(id)["response"]
        file_yaml["slices"]["sliced"]["slice-id"] = id_slice
        file_yaml["slices"]["sliced"]["slice-part-id"]["slice-controller-id"] = id["slice-part-id"]["slice-controller-id"]
        file_yaml["slices"]["sliced"]["slice-part-id"]["uuid"] = id["slice-part-id"]["uuid"]
        tuple = {"URL": url, "Slice": file_yaml, "dc_slice_2": dc_slice_2}
        files.append(tuple)
    #return files and urls
    return files


def populate_dc_file(request_file, controller, dc_file, name, alternative):
    #logs.logger.info(yaml.dump(alternative))
    try:
            #     #replacing the vdus in the file of controller
        file = open(dc_file, 'r')
        file_yaml = yaml.load(file.read())

        file_yaml["slices"]["sliced"]["id"] = request_file["slices"]["sliced"]["id"]
        file_yaml["slices"]["sliced"]["name"] = request_file["slices"]["sliced"]["name"]
        file_yaml["slices"]["sliced"]["short-name"] = request_file["slices"]["sliced"]["short-name"]
        file_yaml["slices"]["sliced"]["vendor"] = request_file["slices"]["sliced"]["vendor"]
        file_yaml["slices"]["sliced"]["slice-requirements"] = request_file["slices"]["sliced"]["slice-requirements"]
        file_yaml["slices"]["sliced"]["slice-lifecycle"] = request_file["slices"]["sliced"]["slice-lifecycle"]
        file_yaml["slices"]["sliced"]["slice-timeframe"] = request_file["slices"]["sliced"]["slice-timeframe"]
        file_yaml["slices"]["sliced"]["description"] = request_file["slices"]["sliced"]["description"]
        file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["VIM"] = alternative["VIM"]
        file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["type"] = alternative["type"]
        file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["monitoring-parameters"] = alternative["monitoring-parameters"]
        file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["name"] = name
                #logs.logger.info(yaml.dump(file_yaml))
    except Exception as e:
        errors = []
        errors.append("Invalid yaml file!")
        if(len(e.args)>2): errors.append(e.args[2])
        else: errors.append(e.args)
        logs.callback(0, errors)
        return "0", 400 
        
                  
            #defining type (master or worker) and ip_address of each vdu
    n = 1
    vdus = []
                
    for vdu in file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["VIM"]["vdus"]:
        try:
            if(vdu["vdu"]["instance-count"]==1): 
                vdu["vdu"]["name"] = vdu["vdu"]["name"]+"-"+str(1)
                vdu["vdu"]["id"] = vdu["vdu"]["name"]
                vdus.append(yaml.load(yaml.dump(vdu))["vdu"])
            else:
                if(vdu["vdu"]["instance-count"]>1):
                    for x in range(0, vdu["vdu"]["instance-count"]):
                        new_vdu = yaml.load(yaml.dump(vdu))["vdu"]
                        new_vdu["name"] = new_vdu["name"]+"-"+str(x+1)
                        new_vdu["id"] = new_vdu["name"]
                        vdus.append(new_vdu)
        except Exception as e:
            logs.logger.error(e)
    file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["VIM"]["vdus"] = vdus
    for vdu in file_yaml["slices"]["sliced"]["slice-part"][0]["dc-slice-part"]["VIM"]["vdus"]:
        try:
            if (re.search('master', vdu["name"], re.IGNORECASE)):
                vdu["type"] = "master"
            else:
                if (re.search('node', vdu["name"], re.IGNORECASE)):
                    vdu["type"] = "worker"
                else:
                    vdu["type"] = None
            vdu["ip"] = "10.10."+controller["controller-id"]+"."+str(n)
            n=n+1
                    #logs.logger.info(yaml.dump(vdu))
        except Exception as e:
            logs.logger.error(e)
    return file_yaml

def select_controller(controller):
 #   logs.logger.info(controller)
    provider = controller['dc-slice-provider']
    controller_id = {"controller-id": None, "ip": None, "port": None}
    """if(provider == 'DC UNICAMP'):
        controller_id["controller-id"] = '2'
        controller_id["ip"] = "necos.dca.fee.unicamp.br"
        controller_id["port"] = "5000"
        file_request = './Yaml/iot_slice-builder-to-dc-slice-controller-unicamp.yaml'
    else:
        if(provider == 'DC Telefonica'):
            controller_id["controller-id"] = '5'
            controller_id["ip"] = "143.106.11.131"
            controller_id["port"] = "5001"
            file_request =  './Yaml/iot_slice-builder-to-dc-slice-controller-telefonica.yaml'
        else:
            if(provider == 'DC UFSCar'):
                controller_id["controller-id"] = '4'
                controller_id["ip"] = controller["ip"]
                controller_id["port"] = "23100"
                file_request = './Yaml/iot_slice-builder-to-dc-slice-controller-telefonica.yaml'
            else:
                if(provider == 'DC UOM'):
                    controller_id["controller-id"] = '6'
                    controller_id["ip"] = controller["ip"]
                    controller_id["port"] = controller["port"]
                    file_request =  './Yaml/iot_slice-builder-to-dc-slice-controller-telefonica.yaml'"""
    if(provider == 'DC GERCOM'):
        controller_id["controller-id"] = '2'
        controller_id["ip"] = "10.126.1.32"
        controller_id["port"] = "5000"
        file_request = './Yaml/iot_slice-builder-to-dc-slice-controller-gercom.yaml'
    else:
        if(provider == 'DC IDC'):
            controller_id["controller-id"] = '4'
            controller_id["ip"] = "10.126.1.187"
            controller_id["port"] = "5000"
            file_request =  './Yaml/iot_slice-builder-to-dc-slice-controller-idc.yaml'
    response = [controller_id, file_request]
    return response
