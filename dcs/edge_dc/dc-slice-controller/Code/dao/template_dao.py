from dao.connection import Connection
from model.template import Template

class TemplateDAO():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None

    def get_msg(self):
        return self.__msg

    # Select Template
    def select_template(self, uuid):
        sql = f"SELECT * FROM Template WHERE uuid='{uuid}' OR name='{uuid}'"

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
        #if returns empty
        self.__msg = f"ERROR: No template with uuid = {uuid} found"
        return 0

    # Insert Template
    def insert_template(self, template):
        uuid = template.get_uuid()
        name = template.get_name()
        version = template.get_version()
        memory = template.get_memory()
        vcpu = template.get_vcpu()
        storage = template.get_storage()
        ip_address = template.get_ip_address()
        path = template.get_path()
        vim_type_name = template.get_vim_type_name()

        sql = f"INSERT INTO Template (`uuid`, `name`, `version`, `memory`, `vcpu`, `storage`, `ip_address`, `path`, `VIM_Type_name`) " + \
              f"VALUES ('{uuid}', '{name}', '{version}', '{memory}', {vcpu}, {storage}, '{ip_address}', '{path}', '{vim_type_name}')"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg = "Template entered successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to insert template"
            return 0

    # Update Template
    def update_template(self, template):
        uuid = template.get_uuid()
        name = template.get_name()
        version = template.get_version()
        memory = template.get_memory()
        vcpu = template.get_vcpu()
        storage = template.get_storage()
        ip_address = template.get_ip_address()
        path = template.get_path()
        vim_type_name = template.get_vim_type_name()

        sql = f"UPDATE Template SET uuid='{uuid}', name='{name}', version='{version}', memory={memory}, vcpu={vcpu}, storage={storage}, " + \
              f"ip_address='{ip_address}', path='{path}', VIM_Type_name='{vim_type_name}' WHERE uuid='{uuid}'"
        # Returns success or error message
        if self.__connection.execute_sql(sql):
            self.__msg ="Template updated successfully!"
            return 1
        else: 
            self.__msg = f"ERROR: Failed to template vim with uuid='{uuid}'"
            return 0


    # Delete Template
    def delete_template(self, uuid):
        sql = f"DELETE FROM Template WHERE uuid='{uuid}'"
        if self.__connection.execute_sql(sql): 
            self.__msg ="Template successfully removed!"
            return 1
        else: 
            self.__msg = f"Failed to remove template with uuid={uuid}"
            return 0

    # Select all templates
    def select_all_templates(self):
        sql = "Select * FROM Template"
        
        results = self.__connection.select_sql(sql)
        if results != 0:
            templates = []
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
                # Add template to list
                templates.append(template)
            return templates

    def select_updated_template(self, name):
        sql = f"select * from Template where name='{name}' order by version desc"
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
        #if returns empty
        self.__msg = f"ERROR: No template with name = {name} found"
        return 0

    def select_template_vm(self, name, version):
        sql = f"select * from Template where name='{name}' and version='{version}'"
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
        #if returns empty
        self.__msg = f"ERROR: No template with name = {name} found"
        print(f"ERROR: No template with name = {name} and version = {version} found")
        return 0