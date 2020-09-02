from pymongo import MongoClient

class Mongo:
    def __init__(self,MONGO_IP_PORT, DB_NAME):

        client = MongoClient(MONGO_IP_PORT)
        self.db = client[DB_NAME]

 #       db = client[DB_NAME]


    # Functions to insert, update, read or delete data to or from db for a specific collection

    def insert(self, document, collection):
        try:
            return self.db[collection].insert_one(document)

        except Exception as e:
            return str(e)


    def update(self, _id, document, collection):
        try:
            return self.db[collection].update_one(_id, document)

        except Exception as e:
            return str(e)


    def read(self, collection):
        try:
            return self.db[collection].find()

        except Exception as e:
            return str(e)


    def read_many(self, data, collection):
        try:
            return self.db[collection].find(data)

        except Exception as e:
            return str(e)


    def read_one(self, data, collection):
        try:
            return self.db[collection].find_one(data)

        except Exception as e:
            return str(e)


    def search(self, data, collection):
        try:
            return self.db[collection].find(data)

        except Exception as e:
            return str(e)


    def remove(self, _id, collection):
        try:
            return self.db[collection].remove(_id, True)

        except Exception as e:
            print(str(e))
            
            
    def count(self, collection):
        try:
            return self.db[collection].count()

        except Exception as e:
            return str(e)            
