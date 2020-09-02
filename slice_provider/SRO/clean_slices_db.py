#!/usr/bin/python3
from neo4j import GraphDatabase
import config, requests

def clean_slices_db():
    slices_db = GraphDatabase.driver(config.neo4j_uri, auth=(config.neo4j_user, config.neo4j_pwd))
    with slices_db.session() as session:
        session.run("MATCH (n) DETACH DELETE n")
    slices_db.close()

def register_controllers():
    r = requests.post(f'{config.slices_db_url}/auxiliar')
    try:
        print(r.json())
    except:
        print(r.text)
    controllers = {'UFG': {1: 'DC', 8: 'NET'},
                   'UNICAMP': {2: 'DC', 9: 'NET'},
                   'UFU': {3: 'DC', 10: 'NET'},
                   'UFSCAR': {4: 'DC', 11: 'NET'},
                   'TELEFONICA': {5: 'DC', 12: 'NET'}, 
                   'UOM': {6: 'DC', 13: 'NET'}, 
                   'UPC': {7: 'DC', 14: 'NET'}}
    yaml = 'controller:\n   controller-id: {}\n   name: {}\n   type: {}'
    for provider, value in controllers.items():
        for _id, _type in value.items():
            stream = yaml.format(_id, provider, _type)
            r = requests.post(f'{config.slices_db_url}/register_controller', headers={'Content-type': 'text/x-yaml'}, data=stream)
            try:
                print(r.json())
            except:
                print(r.text)

clean_slices_db()
register_controllers()