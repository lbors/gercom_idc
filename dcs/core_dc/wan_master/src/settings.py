CONTROLLER_ID = 9

MYSQL_USER = "necos"
MYSQL_PASSWORD = 'wanPass!'
#MYSQL_HOST = 'wan-master-mysql'
MYSQL_HOST = '10.126.1.32'
MYSQL_PORT = "3307"
MYSQL_DB = "wan_slice_controller"

AGENTS_TIMEOUT = 5


AGENTS = {
    2 : {
       "agent_address" : "10.126.1.32",
       "agent_port" : 3030,
       "description" : "gercom wan agent",
       "tunnel_address" : "",
   },

    4 : {
    "agent_address" : "10.126.1.187",
    "agent_port" : 3030,
    "description" : "idc wan agent",
    "tunnel_address" : "",
    }
}
'''
   5 : {
       "agent_address" : "ip-address",
       "agent_port" : 3030,
       "description" : "5tonic wan agent",
       "tunnel_address" : ""
   },

   6 : {
    "agent_address" : "ip-address",
    "agent_port" : 3030,
    "description" : "uom wan agent",
    "tunnel_address" : "",
    }
}
'''

