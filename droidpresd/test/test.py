# simple test program (from the XML-RPC specification)
import threading
import time

from xmlrpclib import ServerProxy, Error
from pprint import pprint

def startTest():
    server = ServerProxy("http://DroidPres:ohjaTho5gah2Ohyoh5Ug@localhost:8888/droidpressrpc2") # local server
    try:
        pprint(len(server.GetRefClientGroup(1)))
        pprint(len(server.GetRefClient(1)))
        pprint(len(server.GetRefProductGroup(1)))
        pprint(len(server.GetRefProduct(1)))
    except Error, v:
        print "ERROR", v


 
for i in xrange(3):
    t = threading.Thread(target=startTest)
    t.run()
