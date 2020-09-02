import os, logging, logging.config

def terminal(component):
    LOGGING = {
        'version': 1,
        'disable_existing_loggers': False,
        'formatters': {
            'f': {
                'format': '[%(levelname)s] - %(message)s' ,# - %(asctime)s - %(module)s.%(funcName)s' ,
            }
        },
        'handlers': {
            'streamH': {
                'level':'INFO',
                'class': 'logging.StreamHandler',
                #'stream': 'ext://sys.stdout',
                'formatter': 'f',
            },
        },
        'root': {
            'handlers': ['streamH'],
            'level':'INFO',
        }
    }
    logging.config.dictConfig(LOGGING)
    logger = logging.getLogger('Necos.SRO')
    logger.debug('Logger started.')
    return logger

def _file(component):
    LOGGING = {
        'version': 1,
        'disable_existing_loggers': False,
        'formatters': {
            'f': {
                'format': '[%(levelname)s] - %(message)s' ,# format': '[%(levelname)s] - %(asctime)s - %(module)s.%(funcName)s - %(message)s' ,
            }
        },
        'handlers': {
            'file': {
                'level':'INFO',
                'class': 'logging.FileHandler',
                'filename': f'logs/{component}.log',
                'mode': 'w',
                'formatter': 'f',
            },
        },
        'root': {
            'handlers': ['file'],
            'level':'INFO',
        }
    }
    logging.config.dictConfig(LOGGING)
    logger = logging.getLogger('Necos.SRO')
    logger.debug('Logger started.')
    return logger