import libvirt
import sys
from slice_creator import logs
from settings import xen_user, xen_host, xen_port

uri = f'xen+ssh://{xen_user}@{xen_host}:{xen_port}/system'

def test_connection():
    conn = libvirt.open(uri)
    if (conn == None):
        logs.logger.error(f"Failed to open connection to '{uri}'", file=sys.stderr)
        exit(1)
    else: 
        logs.logger.info("Connected successfully!")
        conn.close()