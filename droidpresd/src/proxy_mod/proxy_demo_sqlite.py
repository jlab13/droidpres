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

import os
from cfg import DEBUG_LEVEL
from pprint import pprint
import sqlite3

DB_PATH = os.path.join(os.path.dirname(__file__), '../db/droidpres_server.sqlite').replace('\\','/')

SQL_CLIENT = 'select * from client'
SQL_PRODUCT = 'select * from product'
SQL_PRODUCT_GR = 'select * from product_group'
SQL_CLIENT_GR = 'select * from client_group'
SQL_TYPEDOC = 'select * from typedoc'
SQL_SETDOC = '''
insert into "document" ("agentdoc_id", "presventype", "client_id", "docdate", "doctime", "itemcount", "mainsumm", "description", "paymentdate", "typedoc_id", "paytype")
values (?,?,?,?,?,?,?,?,?,?,?)
'''
SQL_SETDOCDET = '''
insert into "document_det" ("document_id", "product_id", "qty", "discount", "price")
values (?,?,?,?,?)
'''
SQL_LOCATION = '''
insert into "location" ("date_location", "provider", "lat", "lon", "accuracy", "agent_id")
values (?,?,?,?,?,?)
'''


Log = lambda str, err=False: pprint(str)

def init(CallBackLog):
    global Log
    Log = CallBackLog

def __Connect():
    if DEBUG_LEVEL > 0:
        Log("Connect to DB:\t%s" % DB_PATH)
    try:
        return sqlite3.connect(DB_PATH)
    except Exception as e:
        Log("Connect:\t%s" % e, True)
        raise

def __Cursor2ArrayMap(cursor):
    arr = []
    for row in cursor:
        i = 0
        map = {}
        for value in row:
            if value :
                if type(value) == unicode:
                    map[cursor.description[i][0]] = value.strip()
                else:
                    map[cursor.description[i][0]] = value
            i += 1
        arr.append(map)
    return arr

def __ExecuteStatement(sql, *params):
    if DEBUG_LEVEL > 4:
        Log("ExecuteStatement:\t%s %s" % (sql, params))
    try:
        con = __Connect()
        cur = con.cursor()
        if params:
            cur.execute(sql, params)
            lastrowid = cur.lastrowid
        else:
            cur.execute(sql)
            lastrowid = cur.lastrowid
        con.commit()
        return lastrowid 
    except Exception as e:
        Log("ExecuteStatement:\t%s" % e, True)
        raise

def __FetchAll(sql, mapF = False, *params):
    if DEBUG_LEVEL > 4:
        Log("FetchAll:\tMapFlag=%s sql='%s' %s" % (mapF, sql, params))
    try:
        con = __Connect()
        cur = con.cursor()
        if params:
            cur.execute(sql, params)
        else:
            cur.execute(sql)
        if mapF:
            result = __Cursor2ArrayMap(cur)
        else:    
            result = cur.fetchall() 
        return result
    except Exception as e:
        Log("FetchAll:\t%s" % e, True)
        raise

def GetRefClientGroup(AgentID):
    return __FetchAll(SQL_CLIENT_GR, True)


def GetRefClient(AgentID):
    return __FetchAll(SQL_CLIENT, True)


def GetRefProduct(AgentID):
    return __FetchAll(SQL_PRODUCT, True)


def GetRefProductGroup(AgentID):
    return __FetchAll(SQL_PRODUCT_GR, True)


def GetRefTypeDoc(AgentID):
    return __FetchAll(SQL_TYPEDOC, True)


def SetDoc(Doc, DocDet):
    agentdoc_id = int("%d%d" % (Doc['agent_id'], Doc['_id']))
    try:
        description = Doc['description']
    except:
        description = None

    doc_id = __ExecuteStatement(SQL_SETDOC, agentdoc_id, Doc['presventype'], Doc['client_id'], Doc['docdate'], Doc['doctime'], Doc['itemcount'],
                                    Doc['mainsumm'], description, Doc['paymentdate'], Doc['typedoc_id'], Doc['paytype'])

    for Det in DocDet:
        __ExecuteStatement(SQL_SETDOCDET, doc_id, Det['product_id'], Det['qty'], 0 , Det['price'])

    return doc_id

def SetLocation(AgentID, location):
    for rec in location:
        __ExecuteStatement(SQL_LOCATION, rec['date_location'], rec['provider'],
                           rec['lat'], rec['lon'], rec['accuracy'], int(AgentID))
    
    return True
