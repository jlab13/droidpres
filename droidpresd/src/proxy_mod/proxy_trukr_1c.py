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


import pythoncom
from win32com.client.dynamic import Dispatch
from pprint import pprint

# V77.Application — версия зависимый ключ;
# V77S.Application — версия зависимый ключ, SQL версия;
# V77L.Application — версия зависимый ключ, локальная версия;
# V77M.Application — версия зависимый ключ, сетевая версия.
V7_APPLICATION  = 'V77.Application'
DB_PATH         = 'C:\\1C_base\\TRUKRDEM'
DP_MODULE       = DB_PATH + '\\ExtForms\\droidpres\\main.ert'   
USER            = 'droidpres'

Log = lambda str, err=False: pprint(str)

def init(CallBackLog):
    global Log
    Log = CallBackLog

def __Connect():
    pythoncom.CoInitialize()
    con = Dispatch(V7_APPLICATION)
    con.Initialize(con.RMTrade, '/D%s /N%s' % (DB_PATH, USER), 'NO_SPLASH_SHOW')
    return con 

def __GetReference(operation, param):
    con = __Connect()
    vl = con.CreateObject('ValueList')
    vl.set('operation', operation)
    vl.set('param', param)
    pr = vl
    if not con.OpenForm('Report', pr, DP_MODULE):
        raise Exception('Error opening: ' + DP_MODULE)
    error = vl.get('error')
    if error:
        raise Exception(error)
    data =  eval(vl.get('result'))
    con = None
    pythoncom.CoUninitialize()
    return data 

def GetRefClientGroup(AgentID): 
    return __GetReference('GetRefClientGroup', AgentID)


def GetRefClient(AgentID): 
    return __GetReference('GetRefClient', AgentID)


def GetRefProductGroup(AgentID):
    return __GetReference('GetRefProductGroup', AgentID)

def GetRefProduct(AgentID): 
    return __GetReference('GetRefProduct', AgentID)

def GetRefTypeDoc(AgentID): 
    return __GetReference('GetRefTypeDoc', AgentID)

def SetDoc(Doc, DocDet):
    con = __Connect()
    lvHead = con.CreateObject('ValueList'); 
    lvDat = con.CreateObject('ValueList');

    for key, value in Doc.items():
        lvHead.AddValue(value, key)
    for rec in DocDet:
        lvDatRec = con.CreateObject('ValueList');
        for key, value in rec.items():
            lvDatRec.AddValue(value, key)
        lvDat.AddValue(lvDatRec)

    doc_id = con.SetDoc(lvHead, lvDat);
    con = None
    pythoncom.CoUninitialize()
    return int(doc_id)
