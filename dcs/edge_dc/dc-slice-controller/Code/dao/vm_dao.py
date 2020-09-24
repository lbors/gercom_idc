from dao.connection import Connection
from model.vm import Vm
from model.template import Template
from dao.template_dao import TemplateDAO
import yaml


template_dao = TemplateDAO()
class VmDAO():
    
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Vm
    def select_vm(self, uuid):
        sql = f"SELECT * FROM VM WHERE uuid='{uuid}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                vm = Vm()
                vm.set_uuid(row[0])
                vm.set_name_yaml(row[1])
                vm.set_name_hypervisor(row[2])
                vm.set_type(row[3])
                vm.set_memory(row[4])
                vm.set_vcpu(row[5])
                vm.set_storage(row[6])
                vm.set_ip_address(row[7])
                vm.set_description(row[8])
                vm.set_slice_part_uuid(row[9])
                vm.set_host_uuid(row[10])
                vm.set_template_name(row[11])
                vm.set_template_version(row[12])
                return vm
        # if returns empty
        self.__msg = f"ERROR: No vm with uuid = {uuid} found"
        return 0

    # Insert Vm
    def insert_vm(self, vm):
        uuid = vm.get_uuid()
        memory = vm.get_memory()
        vcpu = vm.get_vcpu()
        storage = vm.get_storage()
        ip_address = vm.get_ip_address()
        slice_part_uuid = vm.get_slice_part_uuid()
        host_uuid = vm.get_host_uuid()
        template_name = vm.get_template_name()
        template_version = template_dao.select_updated_template(template_name).get_version()
        name_yaml = vm.get_name_yaml()
        type = vm.get_type()
        name_hypervisor = vm.get_name_hypervisor()
        description = vm.get_description()

        sql = f"INSERT INTO VM (`uuid`, `name_yaml`, `name_hypervisor`, `memory`, `vcpu`, `storage`, `ip_address`, `Slice_Part_id`, `Host_uuid`, `Template_name`, `Template_version`, `description`, `type`) " + \
              f"VALUES ('{uuid}', '{name_yaml}', '{name_hypervisor}', '{memory}', '{vcpu}', '{storage}', '{ip_address}', '{slice_part_uuid}', '{host_uuid}', '{template_name}', '{template_version}', '{description}', '{type}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "VM entered successfully!"
            return 1
        else:
            self.__msg = f"ERROR: Failed to insert vm"
            return 0

    # Update Vm
    def update_vm(self, vm):
        uuid = vm.get_uuid()
        memory = vm.get_memory()
        vcpu = vm.get_vcpu()
        storage = vm.get_storage()
        ip_address = vm.get_ip_address()
        slice_part_uuid = vm.get_slice_part_uuid()
        host_uuid = vm.get_host_uuid()
        template_name = vm.get_template_name()
        template_version = template_dao.select_updated_template(template_name).get_version()
        name_yaml = vm.get_name_yaml()
        type = vm.get_type()
        name_hypervisor = vm.get_name_hypervisor()
        description = vm.get_description()

        sql = f"UPDATE VM SET uuid='{uuid}', name_yaml='{name_yaml}', name_hypervisor='{name_hypervisor}', memory='{memory}', vcpu='{vcpu}', storage='{storage}', ip_address='{ip_address}', " + \
              f"Slice_Part_id='{slice_part_uuid}', Host_uuid='{host_uuid}', Template_name='{template_name}', Template_version='{template_version}', description='{description}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "VM updated successfully!"
            return 1
        else:
            self.__msg = f"ERROR: Failed to update vm with uuid='{uuid}'"
            return 0

    # Delete Vm
    def delete_vm(self, uuid):
        sql = f"DELETE FROM VM WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql):
            self.__msg = "VM successfully removed!"
            return 1
        else:
            self.__msg = f"Failed to remove vm with uuid={uuid}"
            return 0

    def delete_vm_by_name(self, name):
        sql = f"DELETE FROM VM WHERE name_hypervisor='{name}'"
        if self.__connection.execute_sql(sql):
            self.__msg = "VM successfully removed!"
            return 1
        else:
            self.__msg = f"Failed to remove vm with name_hypervisor={name}"
            return 0

    # Select all vms
    def select_all_vms(self):
        sql = "Select * FROM VM"

        results = self.__connection.select_sql(sql)
        if results != 0:
            vms = []
            for row in results:
                vm = Vm()
                vm.set_uuid(row[0])
                vm.set_name_yaml(row[1])
                vm.set_name_hypervisor(row[2])
                vm.set_type(row[3])
                vm.set_memory(row[4])
                vm.set_vcpu(row[5])
                vm.set_storage(row[6])
                vm.set_ip_address(row[7])
                vm.set_description(row[8])
                vm.set_slice_part_uuid(row[9])
                vm.set_host_uuid(row[10])
                vm.set_template_name(row[11])
                vm.set_template_version(row[12])
                # Add vm to list
                vms.append(vm)
            return vms

    # delete all vms
    def truncate_vm(self, slice_part_uuid):
        sql = f"DELETE FROM VM WHERE Slice_Part_id='{slice_part_uuid}'"
        if self.__connection.execute_sql(sql):
            self.__msg = "VMs successfully removed!"
            return 1
        else:
            self.__msg = f"Failed to truncate VM"
            return 0

    def get_template(self, vm):
        sql = f"Select * FROM Template WHERE name='{vm.Template_name}' and version='{vm.Template_version}'"
        results = self.__connection.select_sql(sql)

        if results != 0:
            for row in results:
                template = Template()
                template.set_uuid(row[0])
                template.set_name(row[1])
                template.set_version(row[2])
                template.set_memory(row[3])
                template.set_vcpu(row[4])
                template.set_storage(row[5])
                template.set_ip_address(row[6])
                template.set_path(row[7])
                template.set_vim_type_name(row[8])
                return template
