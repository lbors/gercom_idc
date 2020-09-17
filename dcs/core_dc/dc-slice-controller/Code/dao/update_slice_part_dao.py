import MySQLdb 
import datetime
from model.slice_part import SlicePart
from model.vm import Vm
from dao.connection import Connection
from dao.vim_dao import VimDAO
from dao.template_dao import TemplateDAO

from settings import db_host, db_user, db_password, db_name

template_dao = TemplateDAO()
class UpdateSlicePartDAO():
    def __init__(self):
        # Connection data
        self.__host = db_host
        self.__user = db_user
        self.__password = db_password
        self.__db = db_name
        self.__error = None  

        self.__msg = None
        self.__connection = MySQLdb.connect(self.__host, self.__user, self.__password, self.__db)
        self.__cursor = self.__connection.cursor()

    def get_msg(self):
        return self.__msg

 # Update Slice Part
    def update_slice_part(self, slice_part = None, slice_vim = None, slice_vms = None):
        slice_part_uuid = slice_part.get_uuid()
        slice_part_valid_from = slice_part.get_valid_from()
        slice_part_valid_until = slice_part.get_valid_until()
        slice_part_user_login = slice_part.get_user_login()
        slice_part_vim_uuid = slice_part.get_vim_uuid()
        slice_part_controller_id = slice_part.get_controller_id()
        slice_part_name = slice_part.get_name()

        vim_uuid = slice_vim.get_uuid()
        vim_dashboard_user = slice_vim.get_dashboard_user()
        vim_dashboard_password = slice_vim.get_dashboard_password()
        vim_type_name = slice_vim.get_vim_type_name()

        updated_vms = self.update_vms(slice_vms)
        if(updated_vms !=0):
            sql_slice = f"UPDATE Slice_Part SET uuid='{slice_part_uuid}', valid_from='{slice_part_valid_from}', valid_until='{slice_part_valid_until}', ip='{None}', port='{None}', " + \
                f"User_login='{slice_part_user_login}', VIM_uuid='{slice_part_vim_uuid}', Controller_id='{slice_part_controller_id}', name='{slice_part_name}' WHERE uuid='{slice_part_uuid}'"
            sql_vim = f"UPDATE VIM SET uuid='{vim_uuid}', dashboard_user='{vim_dashboard_user}', dashboard_password='{vim_dashboard_password}', " + \
                f" VIM_Type_name='{vim_type_name}' WHERE uuid='{vim_uuid}'"
            try: 
                print("pSQL = " + f"'{sql_vim}'")
                self.__cursor.execute(sql_vim)
                print("pSQL = " + f"'{sql_slice}'")
                self.__cursor.execute(sql_slice)                
            except:
                #print("Error: Could not execute SQL")
                print(f'ERRO = {self.__connection.error()}')
                self.__connection.rollback()
                self.__connection.close
                self.__msg = f"ERROR: Failed to update slice_part with uuid='{slice_part_uuid}'"
                return 0
            self.__connection.commit()
            self.__connection.close
            self.__msg ="Slice_Part updated successfully!"
            return 1
        else:
            return 0   
        
    def update_vms(self, vms):
        pSql = ''
        for vm in vms:
            uuid = vm.get_uuid()
            memory = vm.get_memory()
            vcpu = vm.get_vcpu()
            storage = vm.get_storage()
            ip_address = vm.get_ip_address()
            volume_path = vm.get_volume_path()
            slice_part_uuid = vm.get_slice_part_uuid()
            host_uuid = vm.get_host_uuid()
            template_name = vm.get_template_name()
            template_version = template_dao.select_updated_template(template_name).get_version()
            name_yaml = vm.get_name_yaml()
            description = vm.get_description()
            type = vm.get_type()
            name_hypervisor = template_name+'-'+template_version+'-'+type+'-'+slice_part_uuid
        

            pSql = f"UPDATE VM SET uuid='{uuid}', name_yaml='{name_yaml}', name_hypervisor='{name_hypervisor }', memory='{memory}', vcpu='{vcpu}', storage='{storage}', ip_address='{ip_address}', " + \
              f"Slice_Part_uuid='{slice_part_uuid}', Host_uuid='{host_uuid}', Template_name='{template_name}', Template_version='{template_version}', description='{description}', type='{type}' WHERE uuid='{uuid}'"
       
            try:
                print("pSQL = " + f"'{pSql}'")
                self.__cursor.execute(pSql)
            except:
                #print("Error: Could not execute SQL")
                self.__connection.rollback()
                self.__connection.close
                self.__msg = f"ERROR = Failed to update vm with uuid='{uuid}'"
                return 0
        return 1
