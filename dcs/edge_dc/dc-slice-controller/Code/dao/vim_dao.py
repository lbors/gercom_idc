from dao.connection import Connection
from model.vim import Vim

class VimDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Vim
    def select_vim(self, uuid):
        sql = f"SELECT * FROM VIM WHERE uuid='{uuid}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                vim = Vim()
                vim.set_uuid(row[0])
                vim.set_dashboard_user(row[1])
                vim.set_dashboard_password(row[2])
                vim.set_vim_type_name(row[3])
                return vim
        #if returns empty
        self.__msg = f"ERROR: No vim with uuid = {uuid} found"
        return 0

    # Insert Vim
    def insert_vim(self, vim):
        uuid = vim.get_uuid()
        dashboard_user = vim.get_dashboard_user()
        dashboard_password = vim.get_dashboard_password()
        vim_type_name = vim.get_vim_type_name()

        sql = f"INSERT INTO VIM (`uuid`, `dashboard_user`, `dashboard_password`,  `VIM_Type_name`)" + \
              f"VALUES ('{uuid}', '{dashboard_user}', '{dashboard_password}', '{vim_type_name}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "VIM entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert VIM"
            return 0

    # Update Vim
    def update_vim(self, vim):
        uuid = vim.get_uuid()
        dashboard_user = vim.get_dashboard_user()
        dashboard_password = vim.get_dashboard_password()
        vim_type_name = vim.get_vim_type_name()

        sql =  f"UPDATE VIM SET uuid='{uuid}', dashboard_user='{dashboard_user}', dashboard_password='{dashboard_password}', " + \
               f" VIM_Type_name='{vim_type_name}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            
            self.__msg ="VIM updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update vim with uuid='{uuid}'"
            return 0

    # Delete Vim
    def delete_vim(self, uuid):
        sql = f"DELETE FROM VIM WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="VIM successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove vim with uuid={uuid}"
            return 0

    # Select all vims
    def select_all_vims(self):
        sql = "Select * FROM VIM"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            vims = []
            for row in results:
                vim = Vim()
                vim.set_uuid(row[0])
                vim.set_dashboard_user(row[1])
                vim.set_dashboard_password(row[2])
                vim.set_vim_type_name(row[3])
                # Add vim to list
                vims.append(vim)
            return vims