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


from SimpleXMLRPCServer import SimpleXMLRPCDispatcher, SimpleXMLRPCRequestHandler
from base64 import b64decode
from log import Log
from cfg import SERVER_RPC_PATH, SERVER_USER, SERVER_PASSWD, DEBUG_LEVEL
from SocketServer import ThreadingTCPServer
try:
    import fcntl
except ImportError:
    fcntl = None


class DroidPresRequestHandler(SimpleXMLRPCRequestHandler):
    rpc_paths = (SERVER_RPC_PATH,)

    def __init__(self, request, client_address, server):
        Log("Connect from:\t%s" % client_address[0], False)
        SimpleXMLRPCRequestHandler.__init__(self, request, client_address, server)

    def parse_request(self):
        if SimpleXMLRPCRequestHandler.parse_request(self):
            if self.__authenticate():
                return True
            else:
                self.send_error(401, 'Authentication failed')
                return False

    def __authenticate(self):
        auth = self.headers.get("Authorization", "")
        if DEBUG_LEVEL > 4:
            Log("Authenticate:\t%s" % auth)
        if not auth:
            return False
        (auth_metod, encode) = auth.split()
        if auth_metod <> 'Basic':
            return False
        (user, passwd) = b64decode(encode).split(':')
        if user == SERVER_USER and passwd == SERVER_PASSWD:  
            return True
        else:
            return False

class ThreadingXMLRPCServer(ThreadingTCPServer, SimpleXMLRPCDispatcher):
    allow_reuse_address = True
    _send_traceback_header = False

    def __init__(self, addr, requestHandler=DroidPresRequestHandler, logRequests=False, allow_none=False, encoding=None, bind_and_activate=True):
        self.logRequests = logRequests

        SimpleXMLRPCDispatcher.__init__(self, allow_none, encoding)
        ThreadingTCPServer.__init__(self, addr, requestHandler, bind_and_activate)

        if fcntl is not None and hasattr(fcntl, 'FD_CLOEXEC'):
            flags = fcntl.fcntl(self.fileno(), fcntl.F_GETFD)
            flags |= fcntl.FD_CLOEXEC
            fcntl.fcntl(self.fileno(), fcntl.F_SETFD, flags)
