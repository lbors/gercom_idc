from sqlalchemy import Column, ForeignKey, Integer, String, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy import create_engine
import time
import logging
from random import randint

from settings import MYSQL_USER, MYSQL_PASSWORD, MYSQL_HOST, MYSQL_DB

Base = declarative_base()
 

connection_string = 'mysql+pymysql://%s:%s@%s:%s/%s' % (
            MYSQL_USER,
            MYSQL_PASSWORD,
            MYSQL_HOST,
            "3306",
            MYSQL_DB
        )

engine = create_engine(connection_string)
DBSession = sessionmaker(bind=engine)

class WanSlice(Base):
    __tablename__ = 'wanslice'
    id = Column(Integer, primary_key=True)
    state = Column(Boolean, default = False)
    tunnel_key = Column(Integer)
    dc_slice_part_id_a = Column(String(50))
    dc_slice_part_id_b = Column(String(50))
    dc_slice_controller_id_a = Column(String(50))
    dc_slice_controller_id_b = Column(String(50))


def used_key(tunnel_key):
    session = DBSession()
    num_slices = session.query(WanSlice).filter(WanSlice.tunnel_key == tunnel_key).count()

    if num_slices == 0:
        return False
    return True

def gen_tunnel_key():
    tunnel_key = randint(1, 500)

    while used_key(tunnel_key):
        tunnel_key = randint(1, 500)
    
    return tunnel_key


def create_wan_slice(dc_slice_part_id_a, dc_slice_part_id_b, dc_slice_controller_id_a, dc_slice_controller_id_b):
    session = DBSession()

    tunnel_key = gen_tunnel_key()

    new_slice = WanSlice(
        tunnel_key = tunnel_key,
        dc_slice_part_id_a = dc_slice_part_id_a,
        dc_slice_part_id_b = dc_slice_part_id_b,
        dc_slice_controller_id_a = dc_slice_controller_id_a,
        dc_slice_controller_id_b = dc_slice_controller_id_b
    )

    session.add(new_slice)
    session.commit()

    return new_slice.id


def set_slice_activated(wan_slice_part_id):
    session = DBSession()

    wan_slice = session.query(WanSlice).filter(WanSlice.id == wan_slice_part_id).first()
    wan_slice.state = True

    session.commit()

def set_slice_deactivated(wan_slice_part_id):
    session = DBSession()

    wan_slice = session.query(WanSlice).filter(WanSlice.id == wan_slice_part_id).first()
    wan_slice.state = False

    session.commit()


def get_wan_slice(wan_slice_part_id):
    session = DBSession()

    wan_slice = session.query(WanSlice).filter(WanSlice.id == wan_slice_part_id).first()

    return wan_slice

def wan_slice_to_dict(wan_slice):
    return{
        "dc_slice_controller_id_a" : wan_slice.dc_slice_controller_id_a,
        "dc_slice_part_id_a" : wan_slice.dc_slice_part_id_a,
        "dc_slice_controller_id_b" : wan_slice.dc_slice_controller_id_b,
        "dc_slice_part_id_b" : wan_slice.dc_slice_part_id_b,
        "tunnel_key" : wan_slice.tunnel_key,
    }

def test_db_connection():
    try:
        conn = engine.connect()
        logging.info(conn.execute("SELECT host FROM INFORMATION_SCHEMA.PROCESSLIST WHERE ID = CONNECTION_ID()").fetchall())
        Base.metadata.create_all(engine)
        return True
    except Exception as e:
        logging.error(e)
        False

def create_db():
    logging.info("Creating all database models")
    Base.metadata.create_all(engine)
    logging.info("Database model creation complete!")
