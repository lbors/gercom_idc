from dao.connection import Connection
from model.cpu import Cpu

class CpuDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Cpu
    def select_cpu(self, uuid):
        sql = f"SELECT * FROM CPU WHERE uuid='{uuid}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                cpu = Cpu()
                cpu.set_uuid(row[0])
                cpu.set_cycles(row[1])
                cpu.set_cores(row[2])
                cpu.set_model(row[3])
                cpu.set_architecture(row[4])
                cpu.set_instruction_set(row[5])
                return cpu
        #if returns empty
        self.__msg = f"ERROR: No cpu with uuid = {uuid} found"
        return 0

    # Insert CPU
    def insert_cpu(self, cpu):
        uuid = cpu.get_uuid()
        cycles = cpu.get_cycles()
        cores =  cpu.get_cores()
        model = cpu.get_model()
        architecture = cpu.get_architecture()
        instruction_set = cpu.get_instruction_set()

        sql = f"INSERT INTO CPU (`uuid`, `cycles`, `cores`, `model`, `architecture`, `instruction_set`) " + \
              f"VALUES ('{uuid}', '{cycles}', '{cores}', '{model}', '{architecture}', '{instruction_set}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "CPU entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert cpu"
            return 0

    # Update CPU
    def update_cpu(self, cpu):
        uuid = cpu.get_uuid()
        cycles = cpu.get_cycles()
        cores =  cpu.get_cores()
        model = cpu.get_model()
        architecture = cpu.get_architecture()
        instruction_set = cpu.get_instruction_set()

        sql = f"UPDATE CPU SET uuid='{uuid}', cycles='{cycles}', cores='{cores}', model='{model}', architecture='{architecture}', " + \
              f"instruction_set='{instruction_set}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="CPU updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update cpu with uuid='{uuid}'"
            return 0

    # Delete CPU
    def delete_cpu(self, uuid):
        sql = f"DELETE FROM CPU WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="CPU successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove cpu with uuid={uuid}"
            return 0

    # Select all cpus
    def select_all_cpus(self):
        sql = "Select * FROM CPU"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            cpus = []
            for row in results:
                cpu = Cpu()
                cpu.set_uuid(row[0])
                cpu.set_cycles(row[1])
                cpu.set_cores(row[2])
                cpu.set_model(row[3])
                cpu.set_architecture(row[4])
                cpu.set_instruction_set(row[5])
                # Add cpu to list
                cpus.append(cpu)
            return cpus