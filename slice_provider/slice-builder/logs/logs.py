import logging
import yaml, os
from settings import log_file, time_file

logging.getLogger('').handlers = []
logging.getLogger('slice_activator').setLevel(logging.DEBUG)
FORMAT = '%(asctime)s - %(funcName)s - %(levelname)s - %(message)s' 
logging.basicConfig(filename=log_file, filemode='w', format=FORMAT)
logger = logging.getLogger('AppServer')
logger.setLevel(logging.DEBUG)
 
def callback(status=None, info=None):
    if(status==1):
        return_yaml = {"Status": "OK", "Response": info}
        logger.info("\n" + yaml.dump(return_yaml, default_style=False, default_flow_style=False))
    else:
        return_yaml = {"Status": "ERROR", "Message": info}
        logger.info("\n" + yaml.dump(return_yaml, default_style=False, default_flow_style=False))
    return yaml.dump(return_yaml, default_style=False, default_flow_style=False)
    
def echo_to_file(string):
    os.system(f"echo '{string}' >> {time_file}")
