import datetime
from model.slice_part import SlicePart
from model.vm import Vm
from dao.connection import Connection
from dao.vim_dao import VimDAO
from dao.template_dao import TemplateDAO
from slice_creator import logs 



class SlicePartDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Slice Part
    def select_slice_part(self, uuid):
        sql = f"SELECT * FROM Slice_Part WHERE id={uuid}"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                slice_part = SlicePart()
                slice_part.set_uuid(row[0])
                slice_part.set_name(row[1])
                slice_part.set_valid_from(row[2])
                slice_part.set_valid_until(row[3])
                slice_part.set_ip(row[4])
                slice_part.set_port(row[5])
                slice_part.set_status(row[6])
                slice_part.set_vim_uuid(row[7])
                slice_part.set_controller_id(row[8])
                #vim = VimDAO().select_vim(row[4])
                slice_part.set_user(row[9])
                
                # Select VMs of slice part
                vms = self.select_slice_part_vms(slice_part.get_uuid())
                slice_part.set_vms(vms)
                return slice_part
        # if returns empty
        self.__msg = f"ERROR: No slice part with uuid = {uuid} found"
        return 0

    # Insert Slice Part
    def insert_slice_part(self, slice_part):
        #uuid = slice_part.get_uuid()
        user_name = slice_part.get_user()
        vim_uuid = slice_part.get_vim_uuid()
        controller_id = slice_part.get_controller_id()
        ip = slice_part.get_ip()
        port = slice_part.get_port()
        name = slice_part.get_name()
        status = slice_part.get_status()
        
        sql = f"INSERT INTO Slice_Part (`name`, `ip`, `port`, `status`, `VIM_uuid`, `Controller_id`, `User_name`) VALUES " + \
              f"('{name}', '{ip}', '{port}', '{status}', '{vim_uuid}', '{controller_id}', '{user_name}')"
        print (sql)
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "Slice part entered successfully!"
            self.select_last(user_name)
            return 1
        else:
            self.__msg = f"ERROR: Failed to insert slice part"
            return 0

    # Update Slice Part
    def update_slice_part(self, slice_part):
        uuid = slice_part.get_uuid()
        user = slice_part.get_user()
        vim_uuid = slice_part.get_vim_uuid()
        controller_id = slice_part.get_controller_id()
        ip = slice_part.get_ip()
        port = slice_part.get_port()
        name = slice_part.get_name()
        status = slice_part.get_status()

        sql = f"UPDATE Slice_Part SET id={uuid}, name='{name}', ip='{ip}', port='{port}', " + \
              f"status='{status}', VIM_uuid='{vim_uuid}', Controller_id='{controller_id}', User_name='{user}'  WHERE id={uuid}"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "Slice part updated successfully!"
            return 1
        else:
            self.__msg = f"ERROR: Failed to update slice part with id={uuid}"
            return 0

    # Delete Slice Part
    def delete_slice_part(self, uuid):
        sql = f"DELETE FROM Slice_Part WHERE id={uuid}"
        if self.__connection.execute_sql(sql):
            self.__msg = "Slice part successfully removed!"
            return 1
        else:
            self.__msg = f"Failed to remove slice part with uuid={uuid}"
            return 0

    # Select all slice parts
    def select_all_slice_parts(self):
        sql = "Select * FROM Slice_Part"

        results = self.__connection.select_sql(sql)
        if results != 0:
            slice_parts = []
            for row in results:
                slice_part = SlicePart()
                slice_part.set_uuid(row[0])
                slice_part.set_name(row[1])
                slice_part.set_valid_from(row[2])
                slice_part.set_valid_until(row[3])
                slice_part.set_ip(row[4])
                slice_part.set_port(row[5])
                slice_part.set_status(row[6])
                slice_part.set_vim_uuid(row[7])
                slice_part.set_controller_id(row[8])
                #vim = VimDAO().select_vim(row[4])
                slice_part.set_user(row[9])
                # Select VMs of slice part
                vms = self.select_slice_part_vms(slice_part.get_uuid())
                slice_part.set_vms(vms)
                # Add slice part to list
                slice_parts.append(slice_part)
            return slice_parts

    def select_slice_part_vms(self, uuid):
        sql = f"Select * FROM VM WHERE Slice_Part_id={uuid}"

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
                vm.set_template(TemplateDAO().select_template_vm(row[11], row[12]))
                # Add vm to list
                vms.append(vm)
            return vms

    # returns the sum of the resources used by slice_part vms (group by Host)
    def sum_vm_usage(self, uuid):
        sql = f"Select Host_uuid, sum(memory), sum(storage), sum(vcpu) FROM VM WHERE Slice_Part_id={uuid} group by Host_uuid"
        result_sql = self.__connection.select_sql(sql)
        results = {}
        if result_sql != 0:
            for row in result_sql:
                if(results.get(row[0]) == None and row[0] != None):
                    results[row[0]] = {
                        "memory": row[1],
                        "storage": row[2],
                        "vcpu": row[3]
                    }
            return results

    # returns the sum of the resources used by all slice_part (group by Host) in the stipulated period of time
    def range_slice(self, valid_from, valid_until):
        sql = f"SELECT id FROM Slice_Part"
       # sql = f"SELECT uuid FROM Slice_Part WHERE (valid_from >= '{valid_from}' and valid_from <= '{valid_until}') or (valid_from <= '{valid_from}' and valid_until > '{valid_until}')"
        results = self.__connection.select_sql(sql)
        alloc = {}
        for slice in results:
            result_vms = self.sum_vm_usage(slice[0])
            for row in result_vms:
                if(alloc.get(row) == None):
                    alloc[row] = {
                        "memory": result_vms[row]["memory"],
                        "storage": result_vms[row]["storage"],
                        "vcpu": result_vms[row]["vcpu"]
                    }
                else:
                    if(result_vms[row]["memory"] != None):
                        alloc[row]["memory"] += result_vms[row]["memory"]
                    if(result_vms[row]["storage"] != None):
                        alloc[row]["storage"] += result_vms[row]["storage"]
                    if(result_vms[row]["vcpu"] != None):
                        alloc[row]["vcpu"] += result_vms[row]["vcpu"]
        return alloc

    def select_last(self, user):
        sql = f"SELECT id FROM Slice_Part WHERE User_name='{user}' ORDER BY id DESC LIMIT 1"
        result_sql = self.__connection.select_sql(sql)
        if result_sql != 0:
            for row in result_sql:
                logs.logger.info(f"Returning slice_part id = {row[0]}")
                return row[0]
