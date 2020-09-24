from flask import Flask, Blueprint, request
import yaml
from functools import wraps
from dao.vim_dao import VimDAO
from dao.slice_part_dao import SlicePartDAO
from dao.vm_dao import VmDAO
from model.slice_part import SlicePart
from model.vim import Vim
from model.vm import Vm
#import logs

class ResourceNotFoundError(Exception):
    pass


def valid_yaml_header(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if request.headers.get("Content-Type") != 'application/x-yaml':
            raise ResourceNotFoundError("Yaml header invalid")
        return f(*args, **kwargs)
    return decorated_function

def valid_yaml_content(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            yaml.load(f)
        except Exception as e:
            raise ResourceNotFoundError("Invalid yaml file")
        return f(*args, **kwargs)
    return decorated_function

def valid_fields_post_slice(dc_slice_part):
    if(dc_slice_part.get("name") and dc_slice_part.get("user") and
        dc_slice_part.get("VIM") and
        dc_slice_part["VIM"].get("VIM_Type_name")):
        if(dc_slice_part.get("vdus") and dc_slice_part["vdus"][0].get("vdu")):
            for vm in dc_slice_part["vdus"]:
                if(vm.get("vdu") and
                    vm["vdu"].get("name") and
                    vm["vdu"].get("ip-address") and
                    vm["vdu"].get("description") and
                    vm["vdu"].get("template_name") and
                    vm["vdu"].get("type")
                    ):
                    pass

                else: return 0
        else: return 0
    else: return 0

    if(dc_slice_part["name"]!=None and dc_slice_part["user"]!=None and 
            dc_slice_part["VIM"]!=None and
            dc_slice_part["VIM"]["name"]!=None):
        for vm in dc_slice_part["vdus"]:
            if(vm["vdu"]["name"]!=None and
                vm["vdu"]["ip"]!=None and
                vm["vdu"]["description"]!=None and
                vm["vdu"]["vdu-image"]!=None and
                vm["vdu"]["type"]!=None):
                pass
            else: return 0
    else: return 0
    
    return 1

def valid_fields_post_host(host):
    if(host.get("memory") and 
        host.get("storage") and 
        host.get("ip_address") and
        host.get("availability") and
        host.get("location") and
        host.get("cpu") and
        host["cpu"].get("cycles") and
        host["cpu"].get("cores") and
        host["cpu"].get("model") and
        host["cpu"].get("architecture") and
        host["cpu"].get("instruction_set")):
        if(host["memory"]!=None and host["storage"]!=None 
            and host["ip_address"]!=None and
            host["availability"]!=None and
            host["location"]!=None and host["cpu"]["cycles"]!=None
            and host["cpu"]["cores"]!=None
            and host["cpu"]["model"]!=None 
            and host["cpu"]["architecture"]!=None
            and host["cpu"]["instruction_set"]!=None):
            pass
        else:
            return 0
    else: return 0
    return 1

def valid_fields_post_template(template):
    if(template.get("name") and 
        template.get("version") and 
        template.get("memory") and
        template.get("vcpu") and
        template.get("storage") and
        template.get("ip_address") and
        template.get("path") and
        template.get("VIM_Type_name")):
        if(template["name"]!=None and template["version"]!=None 
            and template["memory"]!=None and
            template["vcpu"]!=None and
            template["storage"]!=None and 
            template["ip_address"]!=None and
            template["path"]!=None and
            template["name"]!=None):
            pass
        else:
            return 0
    else: return 0
    return 1

def valid_fields_post_user(user):
    if(user.get("name")):
        if(user["name"]!=None):
            pass
        else:
            return 0
    else: return 0
    return 1

def valid_fields_vim_type(vim_type):
    if(vim_type.get("name")):
        if(vim_type["name"]!=None):
            pass
        else: 
            return 0
    else: return 0
    return 1
