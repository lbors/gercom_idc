from dao.slice_part_dao import SlicePartDAO
from dao.vm_dao import VmDAO
from dao.host_dao import HostDAO
from dao.cpu_dao import CpuDAO
from dao.template_dao import TemplateDAO

class HandleSliceCreator(object):
    
    def __init__(self):
        self.__slice_part_dao = SlicePartDAO()
        self.__host_dao = HostDAO()
        self.__cpu_dao = CpuDAO()
        self.__vm_dao = VmDAO()
        self.__template_dao = TemplateDAO()

    
    def distribute_vms_insert(self, vms, valid_from, valid_until):
        resource_usage = self.__slice_part_dao.range_slice(
            valid_from, valid_until)
        hosts = self.__host_dao.select_all_hosts()
        for vm in vms:
            insert = 0
            for host in hosts:
                cpu = self.__cpu_dao.select_cpu(host.get_CPU_uuid())
                if(resource_usage.get(host.get_uuid()) == None):
                    if(vm.get_memory() <= host.get_memory() 
                    and vm.get_storage() <= host.get_storage() 
                    and vm.get_vcpu() <= cpu.get_cores()):
                        insert = 1
                        vm.set_host_uuid(host.get_uuid())
                        resource_usage[host.get_uuid()]= {
                            "memory": vm.get_memory(),
                            "storage": vm.get_storage(),
                            "vcpu": vm.get_vcpu()
                        }
                        break
                    
                else:
                    if(vm.get_memory() <= (host.get_memory() - resource_usage[host.get_uuid()]["memory"])
                    and vm.get_storage() <= (host.get_storage() - resource_usage[host.get_uuid()]["storage"])
                    and vm.get_vcpu() <= (cpu.get_cores() - resource_usage[host.get_uuid()]["vcpu"])):
                        insert = 1
                        vm.set_host_uuid(host.get_uuid())
                        resource_usage[host.get_uuid()]["memory"] += vm.get_memory()
                        resource_usage[host.get_uuid()]["storage"] += vm.get_storage()
                        resource_usage[host.get_uuid()]["vcpu"] += vm.get_vcpu()
                        break
    
                if(insert == 0): return 0
        return vms

    def distribute_vms_update(self, vms, valid_from, valid_until):
        resource_usage = self.__slice_part_dao.range_slice(
            valid_from, valid_until)
        hosts = self.__host_dao.select_all_hosts()
        for vm in vms:
            old_vm = self.__vm_dao.select_vm(vm.get_uuid())
            if(old_vm==0): return 0
            resource_usage[old_vm.get_host_uuid()]["memory"] -= old_vm.get_memory()
            resource_usage[old_vm.get_host_uuid()]["storage"] -= old_vm.get_storage()
            resource_usage[old_vm.get_host_uuid()]["vcpu"] -= old_vm.get_vcpu()

        for vm in vms:
            update = 0
            for host in hosts:
                cpu = self.__cpu_dao.select_cpu(host.get_CPU_uuid())
                if(resource_usage.get(host.get_uuid()) == None):
                    if(vm.get_memory() <= host.get_memory() 
                    and vm.get_storage() <= host.get_storage() 
                    and vm.get_vcpu() <= cpu.get_cores()):
                        update = 1
                        vm.set_host_uuid(host.get_uuid())
                        resource_usage[host.get_uuid()]= {
                            "memory": vm.get_memory(),
                            "storage": vm.get_storage(),
                            "vcpu": vm.get_vcpu()
                        }
                        break
                    
                else:
                    if(vm.get_memory() <= (host.get_memory() - resource_usage[host.get_uuid()]["memory"])
                    and vm.get_storage() <= (host.get_storage() - resource_usage[host.get_uuid()]["storage"])
                    and vm.get_vcpu() <= (cpu.get_cores() - resource_usage[host.get_uuid()]["vcpu"])):
                        update = 1
                        vm.set_host_uuid(host.get_uuid())
                        resource_usage[host.get_uuid()]["memory"] += vm.get_memory()
                        resource_usage[host.get_uuid()]["storage"] += vm.get_storage()
                        resource_usage[host.get_uuid()]["vcpu"] += vm.get_vcpu()
                        break
                if(update == 0): return 0
        return vms