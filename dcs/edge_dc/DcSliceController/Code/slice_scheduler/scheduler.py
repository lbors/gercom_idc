from dao.connection import Connection
from datetime import datetime
from datetime import date
from datetime import timedelta
from apscheduler.schedulers.asyncio import AsyncIOScheduler
import time
import os
import _thread

try:
    import asyncio
except ImportError:
    import trollius as asyncio

class SliceScheduler():
    def __init__(self):
        self.__connection = Connection()
        self.__msg = None
        self.start_scheduler()


    def select_slice(self):
        now = datetime.now()
        after = now + timedelta(seconds=60)
        sql = f"SELECT uuid FROM Slice WHERE valid_from >= '{now}' and valid_from <= '{after}' and status = 'accepted'"
        results = self.__connection.select_sql(sql)
        return results

    def configure(self, uuid):
        print("active %r" % uuid)
        
    def start_slice(self):
        results = self.select_slice()
        if len(results) != 0:
            for row in results:
                try:
                    _thread.start_new_thread(self.configure, (row, ))
                except:
                    print ("Error: unable to start thread")

        else:
            print(f"No slice with selected time found")

    def start_scheduler(self):
        scheduler = AsyncIOScheduler()
        scheduler.add_job(self.start_slice, 'interval', seconds=60, id='start_slice')
        
        scheduler.start()
        print('Press Ctrl+{0} to exit'.format('Break' if os.name == 'nt' else 'C'))
        try:
            asyncio.get_event_loop().run_forever()
        except (KeyboardInterrupt, SystemExit):
            pass 
