import paramiko, socket, time
from slice_creator import logs

class SSH:
    def __init__(self, hostname, username, password, port=22):
        self.ssh = paramiko.SSHClient()
        self.ssh.load_system_host_keys()
        self.ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        cont = 0
        timeout = 300
        logs.logger.info(f"Waiting for port {port} ...")
        while(self.isOpen(hostname, port) == False):
            if(cont<timeout):
                time.sleep(2)
                cont+=2
            else:
                logs.logger.info("ERROR: Timeout exceeded")
                return
        logs.logger.info(f"ssh service is up after {cont} seconds")
        self.ssh.connect(hostname=hostname,username=username,password=password, port=port)
    ## 

    def exec_cmd(self,cmd):
        stdin,stdout,stderr = self.ssh.exec_command(cmd)
        if(stderr.channel.recv_exit_status() != 0):
            logs.logger.info(stderr.read().decode(encoding='UTF-8'))
        else:
            output = stdout.read().decode(encoding='UTF-8')
            logs.logger.info(output)
            return output
    
    def close(self):
        self.ssh.close()

    def isOpen(self, ip, port):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((ip, int(port)))
            s.shutdown(2)
            return True
        except:
            return False