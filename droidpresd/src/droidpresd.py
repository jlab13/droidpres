#!/usr/bin/env python
#-*- coding: utf-8 -*-"

######################################################################
# Copyright (c) 2010 Eugene Vorobkalo.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the GNU Public License v2.0
# which accompanies this distribution, and is available at
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
# 
# Contributors:
#     Eugene Vorobkalo - initial API and implementation
######################################################################

import sys
from lib.daemon import Daemon
from lib.xmlrpcserver import ThreadingXMLRPCServer
from lib.droidpres_instance import DroidPresInstance
from lib.log import Log
from cfg import SERVER_ADRES, SERVER_PORT

server = None

def start():
    global server 
    server = ThreadingXMLRPCServer(addr=(SERVER_ADRES, SERVER_PORT))
    server.register_introspection_functions()
    server.register_instance(DroidPresInstance())
    Log('Start server on %s:%s' % (SERVER_ADRES, SERVER_PORT))
    server.serve_forever()

class ServerDaemon(Daemon):
    def run(self):
        start()

if __name__ == '__main__':
    if sys.platform == 'linux2OFF':
        daemon = ServerDaemon(pidfile='/var/run/droidpresd.pid', stdout='/var/log/droidpresd.log', stderr='/var/log/droidpresd.log')
        if len(sys.argv) == 2:
            if 'start' == sys.argv[1]:
                daemon.start()
            elif 'stop' == sys.argv[1]:
                daemon.stop()
            elif 'restart' == sys.argv[1]:
                daemon.restart()
            else:
                print "Unknown command."
                print "usage: %s start|stop|restart" % sys.argv[0]
                sys.exit(2)
            sys.exit(0)
        else:
            print "usage: %s start|stop|restart" % sys.argv[0]
            sys.exit(2)
    else:
        try:
            start()
        except KeyboardInterrupt:
            Log('^C received, shutting down server')
            server.socket.close()
