import logging
import yaml
from settings import log_file

logging.getLogger('').handlers = []
logging.getLogger('soa').setLevel(logging.DEBUG)
FORMAT = '%(asctime)s - %(funcName)s - %(levelname)s - %(message)s' 
logging.basicConfig(filename=log_file, filemode='w', format=FORMAT)
logger = logging.getLogger('AppServer')
logger.setLevel(logging.DEBUG)


def callback(status=None, info=None):
    if status == 1:
        return_yaml = {"Status": "OK", "Response": info}
        logger.info("\n" + yaml.dump(return_yaml))
    else:
        return_yaml = {"Status": "ERROR", "Message": info}
        logger.error("\n" + yaml.dump(return_yaml))
    return yaml.dump(return_yaml)
