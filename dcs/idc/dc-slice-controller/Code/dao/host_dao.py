from dao.connection import Connection
from model.host import Host

class HostDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Host
    def select_host(self, uuid):
        sql = f"SELECT * FROM Host WHERE uuid='{uuid}'"

        results = self.__connection.select_sql(sql)
        if results != 0:
            for row in results:
                host = Host()
                host.set_uuid(row[0])
                host.set_memory(row[1])
                host.set_storage(row[2])
                host.set_ip_address(row[3])
                host.set_hostname(row[4])
                host.set_availability(row[5])
                host.set_location(row[6])
                host.set_CPU_uuid(row[7])
                return host
        #if returns empty
        self.__msg = f"ERROR: No host with uuid = {uuid} found"
        return 0

    # Insert Host
    def insert_host(self, host):
        uuid = host.get_uuid()
        memory = host.get_memory()
        storage = host.get_storage()
        ip_address = host.get_ip_address()
        hostname = host.get_hostname()
        availability = host.get_availability()
        location = host.get_location()
        CPU_uuid = host.get_CPU_uuid()

        sql = f"INSERT INTO Host (`uuid`, `memory`, `storage`, `ip_address`, `hostname`, `availability`, `location`, `CPU_uuid`) VALUES ('{uuid}', '{memory}', '{storage}', '{ip_address}', '{hostname}', '{availability}', '{location}', '{CPU_uuid}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "Host entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert host"
            return 0

    # Update Host
    def update_host(self, host):
        uuid = host.get_uuid()
        memory = host.get_memory()
        storage = host.get_storage()
        ip_address = host.get_ip_address()
        hostname = host.get_hostname()
        availability = host.get_availability()
        location = host.get_location()
        CPU_uuid = host.get_CPU_uuid()

        sql =  f"UPDATE Host SET uuid='{uuid}', memory='{memory}', storage='{storage}', ip_address='{ip_address}' , hostname='{hostname}', availability='{availability}', location='{location}', CPU_uuid='{CPU_uuid}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="Host updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to update host with uuid='{uuid}'"
            return 0

    # Delete Host
    def delete_host(self, uuid):
        sql = f"DELETE FROM Host WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="Host successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove host with uuid={uuid}"
            return 0

    # Select all hosts
    def select_all_hosts(self):
        sql = "Select * FROM Host"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            hosts = []
            for row in results:
                host = Host()
                host.set_uuid(row[0])
                host.set_memory(row[1])
                host.set_storage(row[2])
                host.set_ip_address(row[3])
                host.set_hostname(row[4])
                host.set_availability(row[5])
                host.set_location(row[6])
                host.set_CPU_uuid(row[7])
                # Add host to list
                hosts.append(host)
            return hosts

    def sum_resources(self, uuid):
        sql = f"Select memory, storage, cores from Host, CPU where Host.CPU_uuid = CPU.uuid and Host.uuid = '{uuid}'"
        result = self.__connection.select_sql(sql)
        resources = {"memory": 0, "storage": 0, "vcpu": 0}
        for row in result:
            resources["memory"] = row[0]
            resources["storage"] = row[1]
            resources["vcpu"] = row[2]
        print(resources)
        return resources
