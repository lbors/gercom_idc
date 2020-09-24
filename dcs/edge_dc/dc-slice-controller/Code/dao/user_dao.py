from dao.connection import Connection
from model.user import User

class UserDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select User
    def select_user(self, name):
        sql = f"SELECT * FROM User WHERE name='{name}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                user = User()
                user.set_uuid(row[0])
                user.set_name(row[1])
                return user
        #if returns empty
        self.__msg = f"ERROR: No user with name = {name} found"
        return 0

    # Insert User
    def insert_user(self, user):
        uuid = user.get_uuid()
        name = user.get_name()

        sql = f"INSERT INTO User (`uuid`, `name`) VALUES ('{uuid}', '{name}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "User entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert user"
            return 0

    # Update User
    def update_user(self, user):
        uuid = user.get_uuid()
        name = user.get_name()
        # Check if user exists
        if(self.select_user(uuid) == 0):
            self.__msg = f"User with name'{name}' does not exist"
            return 0

        sql =  f"UPDATE User SET uuid='{uuid}', name='{name}' WHERE name='{name}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="User updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update user with uuid='{uuid}'"
            return 0

    # Delete User
    def delete_user(self, name):
        sql = f"DELETE FROM User WHERE name='{name}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="User successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove user with name={name}"
            return 0

    # Select all users
    def select_all_users(self):
        sql = "Select * FROM User"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            users = []
            for row in results:
                user = User()
                user.set_uuid(row[0])
                user.set_name(row[1])
                # Add user to list
                users.append(user)
            return users