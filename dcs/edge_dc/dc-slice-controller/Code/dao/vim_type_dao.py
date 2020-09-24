from dao.connection import Connection
from model.vim_type import VimType

class VimTypeDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select VimType
    def select_vim_type(self, uuid):
        sql = f"SELECT * FROM VIM_Type WHERE uuid='{uuid}' OR name='{uuid}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                vim_type = VimType()
                vim_type.set_uuid(row[0])
                vim_type.set_name(row[1])
                return vim_type
        
        #if returns empty
        self.__msg = f"ERROR: No vim type with uuid = {uuid} found"
        return 0

    # Insert VimType
    def insert_vim_type(self, vim_type):
        uuid = vim_type.get_uuid()
        name = vim_type.get_name()
        sql = f"INSERT INTO VIM_Type (`uuid`, `name`) VALUES ('{uuid}', '{name}')"

        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "VIM_Type entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert VIM_Type: {vim_type}"
            return 0

    # Update VimType
    def update_vim_type(self, vim_type):
        uuid = vim_type.get_uuid()
        name = vim_type.get_name()

        sql =  f"UPDATE VIM_Type SET uuid='{uuid}', name='{name}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="VIM Type updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update vim type with uuid='{uuid}'"
            return 0

    # Delete VimType
    def delete_vim_type(self, uuid):
        sql = f"DELETE FROM VIM_Type WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="VIM Type successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove vim type with uuid={uuid}"
            return 0

    # Select all vim_types
    def select_all_vim_types(self):
        sql = "Select * FROM VIM_Type"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            vim_types = []
            for row in results:
                vim_type = VimType()
                vim_type.set_uuid(row[0])
                vim_type.set_name(row[1])
                # Add vim_type to list
                vim_types.append(vim_type)
            return vim_types