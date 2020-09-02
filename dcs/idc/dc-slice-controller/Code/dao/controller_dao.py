from dao.connection import Connection
from model.controller import Controller

class ControllerDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Controller
    def select_controller(self, id):
        sql = f"SELECT * FROM Controller WHERE id='{id}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                controller = Controller()
                controller.set_id(row[0])
                controller.set_role(row[1])
                controller.set_configuration_protocol(row[2])
                controller.set_configuration_interface(row[3])
                controller.set_provider(row[4])
                return controller
        #if returns empty
        self.__msg = f"ERROR: No controller with id = {id} found"
        return 0

    # Insert CPU
    def insert_controller(self, controller):
        id = controller.get_id()
        role = controller.get_role()
        configuration_protocol = controller.get_configuration_protocol()
        configuration_interface = controller.get_configuration_interface()
        provider = controller.get_provider()

        sql = f"INSERT INTO Controller (`id`, `role`, `configuration_protocol`, `configuration_interface`, `provider`) " + \
              f"VALUES ('{id}', '{role}', '{configuration_protocol}', '{configuration_interface}', '{provider}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "Controller entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert controller"
            return 0

    # Update Controller
    def update_controller(self, controller):
        id = controller.get_id()
        role = controller.get_role()
        configuration_protocol = controller.get_configuration_protocol()
        configuration_interface = controller.get_configuration_interface()
        provider = controller.get_provider()

        sql = f"UPDATE Controller SET id='{id}', role='{role}', configuration_protocol='{configuration_protocol}', configuration_interface='{configuration_interface}', " + \
              f"provider='{provider}' WHERE id='{id}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="Controller updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update controller with id='{id}'"
            return 0

    # Delete Controller
    def delete_controller(self, id):
        sql = f"DELETE FROM Controller WHERE id='{id}'"
        results = self.__connection.execute_sql(sql)
        if (results !=0 ): 
            self.__msg ="Controller successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove controller with id={id}"
            return 0

    # Select all controllers
    def select_all_controllers(self):
        sql = "Select * FROM Controller"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                controller = Controller()
                controller.set_id(row[0])
                controller.set_role(row[1])
                controller.set_configuration_protocol(row[2])
                controller.set_configuration_interface(row[3])
                controller.set_provider(row[4])
                # Add controller to list
                return controller
            return 0

    def select_provider(self, provider):
        sql = f"SELECT * FROM Controller WHERE provider='{provider}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                controller = Controller()
                controller.set_id(row[0])
                controller.set_role(row[1])
                controller.set_configuration_protocol(row[2])
                controller.set_configuration_interface(row[3])
                controller.set_provider(row[4])
                return controller
        #if returns empty
        self.__msg = f"ERROR: No controller with provider = {provider} found"
        return 0
    