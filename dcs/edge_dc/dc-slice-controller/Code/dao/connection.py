import MySQLdb 
from settings import db_user, db_password, db_host, db_name 
from slice_creator import logs
 
class Connection(object): 
    def __init__(self):
        # Connection data
        self.__host = db_host
        self.__user = db_user
        self.__password = db_password
        self.__db = db_name
        self.__error = None  

    # Executes the SQL commands in the database
    def execute_sql(self, pSql):
        logs.logger.info("pSQL = " + f"'{pSql}'")
        # Connects to the database
        connection = MySQLdb.connect(self.__host, self.__user, self.__password, self.__db)
        cursor = connection.cursor() 
        self.__error = None

        try:
            cursor.execute(pSql)
            connection.commit()
            return 1
        except:
            #print("Error: Could not execute SQL")
            self.__error = connection.error
            logs.logger.error(f'{self.__error}')
            connection.rollback()
            return 0
        finally:
            connection.close
            

    def select_sql(self, pSql):
        logs.logger.info("pSQL = " + f"'{pSql}'")
        # Connects to the database
        connection = MySQLdb.connect(self.__host, self.__user, self.__password, self.__db)
        cursor = connection.cursor()
        self.__error = None
        
        try:
            cursor.execute(pSql)
            results = cursor.fetchall()
            return results
        except:
            logs.logger.error("Error: Could not fetch data")
            self.__error = connection.error
            logs.logger.error(f'{self.__error}')
            return 0
        finally:
            connection.close

    def get_error(self):
        return self.__error

    