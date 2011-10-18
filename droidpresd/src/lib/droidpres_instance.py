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


from xmlrpclib import Binary
from os import lstat

from log import Log
from cfg import PROXY_MODULE, DEBUG_LEVEL, APK_FILENAME, GetApkVersion

# Модуль посредник для обменна данными
proxy = __import__('proxy_mod.' + PROXY_MODULE, fromlist=['proxy_mod'])
try:
    proxy.init(Log)
except:
    pass

_apk_size       = 0
_apk_time       = 0
_apk_version    = 0

class DroidPresInstance:
    ERR_AgentID     = u'Agent ID is not set'
    ERR_DocEmpty    = u'A document obtained by not fully'

    def GetUpdateApp(self, version):
        global _apk_size, _apk_time, _apk_version
        try:
            apk_state = lstat(APK_FILENAME)
            if apk_state.st_ctime <> _apk_time or apk_state.st_size <> _apk_size:
                _apk_time = apk_state.st_ctime 
                _apk_size = apk_state.st_size
                _apk_version = GetApkVersion()

            if DEBUG_LEVEL > 0:
                Log("GetUpdateApp:\t Client version=%d, Current version: %d" % (version, _apk_version))
            if version < _apk_version:
                if DEBUG_LEVEL > 1:
                    Log("GetUpdateApp:\tSend ubdate APK v: %d" % _apk_version)
                with open(APK_FILENAME, "rb") as handle:
                    return Binary(handle.read())
            else:
                if DEBUG_LEVEL > 1:
                    Log("GetUpdateApp:\tUpdate does not require")
                return Binary('0')
        except Exception as e:
            Log(e, True)
            raise

    def GetRefClientGroup(self, AgentID):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)
            if DEBUG_LEVEL > 0:
                Log("GetRefClientGroup:\tAgentID=%d" % AgentID)
            data = proxy.GetRefClientGroup(AgentID)
            if DEBUG_LEVEL > 1:
                Log("GetRefClientGroup:\tSend %d records to AgentID=%d" % (len(data), AgentID))
            return data
        except AttributeError as e:
            Log(e, True)
            return [0]
        except Exception as e:
            Log(e, True)
            raise

    def GetRefClient(self, AgentID):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)
            if DEBUG_LEVEL > 0:
                Log("GetRefClient:\tAgentID=%d" % AgentID)
            data = proxy.GetRefClient(AgentID)
            if DEBUG_LEVEL > 1:
                Log("GetRefClient:\tSend %d records to AgentID=%d" % (len(data), AgentID))
            return data
        except AttributeError as e:
            Log(e, True)
            return [0]
        except Exception as e:
            Log(e, True)
            raise

    def GetRefProduct(self, AgentID):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)
            if DEBUG_LEVEL > 0:
                Log("GetRefProduct:\tAgentID=%d" % AgentID)
            data = proxy.GetRefProduct(AgentID)
            if DEBUG_LEVEL > 1:
                Log("GetRefProduct:\tSend %d records to AgentID=%d" % (len(data), AgentID))
            return data
        except AttributeError as e:
            Log(e, True)
            return [0]
        except Exception as e:
            Log(e, True)
            raise

    def GetRefProductGroup(self, AgentID):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)
            if DEBUG_LEVEL > 0:
                Log("GetRefProductGroup:\tAgentID=%d" % AgentID)
            data = proxy.GetRefProductGroup(AgentID)
            if DEBUG_LEVEL > 1:
                Log("GetRefProductGroup:\tSend %d records to AgentID=%d" % (len(data), AgentID))
            return data
        except AttributeError as e:
            Log(e, True)
            return [0]
        except Exception as e:
            Log(e, True)
            raise

    def GetRefTypeDoc(self, AgentID):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)
            if DEBUG_LEVEL > 0:
                Log("GetRefTypeDoc:\tAgentID=%d" % AgentID)
            data = proxy.GetRefTypeDoc(AgentID)
            if DEBUG_LEVEL > 1:
                Log("GetRefTypeDoc:\tSend %d records to AgentID=%d" % (len(data), AgentID))
            return data
        except AttributeError as e:
            Log(e, True)
            return [0]
        except Exception as e:
            Log(e, True)
            raise

    def SetDoc(self, Doc, DocDet):
        try:
            if not Doc or not DocDet:
                raise Exception(self.ERR_DocEmpty)

            if DEBUG_LEVEL > 0:
                Log("SetDoc:\t AgentID=%d DocID=%d DocDate=%s LengthDet=%d" % (Doc['agent_id'], Doc['_id'], Doc['docdate'], len(DocDet)))
            return proxy.SetDoc(Doc, DocDet)
        except AttributeError as e:
            Log(e, True)
            return 0
        except Exception as e:
            Log(e, True)
            raise
    
    def SetLocation(self, AgentID, location):
        try:
            if not AgentID:
                raise Exception(self.ERR_AgentID)

            if DEBUG_LEVEL > 0:
                Log("SetLocation:\t AgentID=%d, Location count=%d" % (AgentID, len(location)))
            return proxy.SetLocation(AgentID, location)
        except AttributeError as e:
            Log(e, True)
            return False
        except Exception as e:
            Log(e, True)
            raise
