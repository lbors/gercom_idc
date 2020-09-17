import logging, yaml, sys, os, time
from settings import log_file, log_time

def echo_to_file(string):
    os.system(f"echo '{string}' >> {log_time}")

logging.getLogger('').handlers = []
logging.getLogger('app_server').setLevel(logging.DEBUG)
FORMAT = '%(asctime)s - %(funcName)s - %(levelname)s - %(message)s' 
logging.basicConfig(filename=log_file, filemode='w', format=FORMAT)
logger = logging.getLogger('AppServer')
logger.setLevel(logging.DEBUG)

def callback(status=None, info=None):
    if(status==1):
        return_yaml = {"Status": "OK", "Response": info}
        logger.info("\n" + yaml.dump(return_yaml, default_style=False, sort_keys=False))
    else:
        return_yaml = {"Status": "ERROR", "Message": info}
        logger.error("\n" + yaml.dump(return_yaml, default_style=False, sort_keys=False))
    return yaml.dump(return_yaml, default_style=False, sort_keys=False)



# echo_to_file("iniciando:")
# inicio = time.time()
# time.sleep(2)
# fim = time.time()

# echo_to_file("O tempo foi de: " + str(fim - inicio))
search_slice_ts = time.time() # start time
time.sleep(2)
search_slice_te = time.time() # end time

echo_to_file("Fetched slice in database in " + str( search_slice_te - search_slice_ts ) + " seconds")
