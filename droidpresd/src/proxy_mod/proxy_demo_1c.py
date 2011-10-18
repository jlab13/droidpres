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
V7_APPLICATION  = 'V77S.Application'
DB_PATH         = 'd:\\src\\1C\\DroidPres\\'


Log = lambda str, err=False: pprint(str)

def init(CallBackLog):
    global Log
    Log = CallBackLog

def __Connect():
    pythoncom.CoInitialize() #@UndefinedVariable
    con = Dispatch(V7_APPLICATION)
    con.Initialize(con.RMTrade, '/D%s' % DB_PATH, 'NO_SPLASH_SHOW')
    return con 

def ValueList2List(list):
    ret_list = []
    for row_nom in range(1,int(list.GetListSize()) + 1):
        row_str = list.GetValue(row_nom)
        ret_list.append(eval(row_str))
    return ret_list  

def GetRefClientGroup(AgentID): 
    con = __Connect()
    result =  ValueList2List(con.GetRefClientGroup(AgentID));
    con = None
    pythoncom.CoUninitialize() #@UndefinedVariable
    return result


def GetRefClient(AgentID): 
    con = __Connect()
    result =  ValueList2List(con.GetRefClient(AgentID));
    con = None
    pythoncom.CoUninitialize() #@UndefinedVariable
    return result


def GetRefProductGroup(AgentID):
    con = __Connect()
    result =  ValueList2List(con.GetRefProductGroup(AgentID));
    con = None
    pythoncom.CoUninitialize() #@UndefinedVariable
    return result

def GetRefProduct(AgentID): 
    con = __Connect()
    result = ValueList2List(con.GetRefProduct(AgentID));
    con = None
    pythoncom.CoUninitialize() #@UndefinedVariable
    return result

def GetRefTypeDoc(AgentID): 
    con = __Connect()
    result =  ValueList2List(con.GetRefTypeDoc(AgentID));
    con = None
    pythoncom.CoUninitialize() #@UndefinedVariable
    return result

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
    pythoncom.CoUninitialize() #@UndefinedVariable
    return int(doc_id)
