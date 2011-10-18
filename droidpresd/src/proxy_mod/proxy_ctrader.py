#!/usr/bin/env python
#-*- coding: utf-8 -*-"

DB_PATH             = 'localhost:alter'
DB_USER             = 'droidpres'
DB_PASSWD           = 'dp654123'
DB_TIMEOUT_MINUTES  = 3


import traceback
import threading
import time
from cfg import DEBUG_LEVEL
from pprint import pprint
import kinterbasdb as fb
fb.init(type_conv=200)

SQL_CLIENT = ''' 
select uniqueid "_ID",
       name,
       debtsumm1,
       debtdays1,
       debtsumm2,
       debtdays2,
       stopshipment,
       routeid category_id, 
       addresslocation address,
       defaultdiscount,
       phone,
       taxcode,
       taxnum,
       okpo,
       mfo,
       bankname,
       dognum,
       fname,
       addresslaw,
       clienttypeid clientgroup_id,
       parentid parent_id
from droidpres_client(?)
'''

SQL_PRODUCT = '''
select uniqueid "_ID",
       (select goods.prodtreeid from goods where id = uniqueid) productgroup_id,
       sortpos sortorder,
       name,
       cast(casesize as double precision) / 100 casesize,
       cast(price as double precision) / 100 price,
       cast(available as double precision) / 100 available
from sprbeepres306_goods(?, current_date)
'''

SQL_PRODUCT_GR = '''
select id "_ID", sname "NAME"
from prodtree
where id >= 100
order by sname
'''

SQL_CLIENT_GR = '''
select id "_ID", name
from clienttype
order by name
'''

SQL_TYPEDOC = '''
select id "_ID",
       sname name,
       days,
       cast(discount as double precision) / 100 discount,
       headerprefix,
       detailsprefix,
       reppattern,
       payclass paytype1or2,
       paytypeid paytype
from sprbpdoctype_tosend
'''

SQL_AGENTSTORE = '''
select storeidsrc, storeiddst, pricetypeid
from bpoptions
where agentid = ?
'''

SQL_LOCATION = '''
insert into "agent_location" ("date_location", "provider", "lat", "lon", "accuracy", "agent_id")
values (?,?,?,?,?,?)
'''


con = None

_proc_params = {}
_writeTPB = (
    fb.isc_tpb_write + #@UndefinedVariable
    fb.isc_tpb_consistency + #@UndefinedVariable
    fb.isc_tpb_nowait #@UndefinedVariable
)
_readTPB = (
    fb.isc_tpb_read + #@UndefinedVariable
    fb.isc_tpb_read_committed + #@UndefinedVariable
    fb.isc_tpb_rec_version +#@UndefinedVariable
    fb.isc_tpb_nowait #@UndefinedVariable
)

Log = lambda str, err=False: pprint(str)

lcTimeOut = threading.Lock()
thTimeOut = threading.Thread()

def init(CallBackLog):
    global Log
    Log = CallBackLog

def chTimeOut():
    global work_time, con
    while 1:
        lcTimeOut.acquire()
        if DEBUG_LEVEL > 5:
            Log("CheckTimeOut:\t%f" % (time.time() - work_time))
        breakFlag = (time.time() - work_time) > (DB_TIMEOUT_MINUTES * 60) and con and not con.closed
        if breakFlag:
            con.close()
            if DEBUG_LEVEL > 0:
                Log("Disconnect from DB:\t%s" % DB_PATH)
        lcTimeOut.release()
        if breakFlag: break
        time.sleep(5)

def _connect():
    global con, work_time, thTimeOut
    work_time = time.time();
    if not con or con.closed:
        if DEBUG_LEVEL > 0:
            Log("Connect to DB:\t%s" % DB_PATH)
        try:
            lcTimeOut.acquire()
            con = fb.connect(dsn=DB_PATH, user=DB_USER, password=DB_PASSWD)
            lcTimeOut.release()
            if not thTimeOut.is_alive():
                thTimeOut = threading.Thread(target=chTimeOut, name="chTimeOut")
                thTimeOut.start()
        except Exception as e:
            if type(e) == fb.OperationalError:
                Log("Connect DataBase:\t%s" % e[1], True)
            else:
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

def __PrepareParamsForProc(proc_name, params):
    data = _proc_params.get(proc_name)
    if not data:
        data = __FetchAll("SELECT RDB$PARAMETER_NAME FROM RDB$PROCEDURE_PARAMETERS WHERE RDB$PARAMETER_TYPE = 0 AND RDB$PROCEDURE_NAME = ?", False, proc_name.upper())
        if DEBUG_LEVEL > 4:
            Log("PrepareParamsForProc:\tget params from db on procedure %s " % (proc_name,))

    if not data:
        raise Exception("Procedure '%s' not found or not input parans" % proc_name)
    else:
        _proc_params[proc_name] = data

    params_in_proc = []

    for param in data:
        params_in_proc.append(param[0].strip().encode('ASCII'))

    exec_params = []

    for p_name in params_in_proc:
        try:
            p_value = params[p_name]
        except:
            p_value = None
        exec_params.append(p_value)

    exec_params = tuple(exec_params)

    if DEBUG_LEVEL > 4:
        Log("PrepareParamsForProc:\treturn %s " % (exec_params,))
    return exec_params

def __ExecuteProcedure(proc_name, params):
    if DEBUG_LEVEL > 4:
        Log("ExecuteProcedure:\t'%s' %s" % (proc_name, params))
    _connect()
    exec_params = __PrepareParamsForProc(proc_name, params)

    cur = con.cursor()
    try:
        cur.callproc(proc_name, exec_params)
        result = cur.fetchone()
        return result
    except Exception as e:
        if type(e) == fb.ProgrammingError:
            Log("ExecuteProcedure DataBase:\t%s" % e[1], True)
        else:
            Log("ExecuteProcedure:\t%s" % e, True)
        raise

def __ExecuteStatement(sql, *params):
    if DEBUG_LEVEL > 4:
        Log("ExecuteStatement:\t%s %s" % (sql, params))
    _connect()
    cur = con.cursor()
    try:
        if params:
            cur.execute(sql, params)
        else:
            cur.execute(sql)
    except Exception as e:
        if type(e) == fb.ProgrammingError:
            Log("ExecuteStatement DataBase:\t%s" % e[1], True)
        else:
            Log("ExecuteStatement:\t%s" % e, True)
        raise

def __FetchAll(sql, mapF = False, *params):
    if DEBUG_LEVEL > 4:
        Log("FetchAll:\tMapFlag=%s sql='%s' %s" % (mapF, sql, params))
    _connect()
    cur = con.cursor()
    try:
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
        if type(e) == fb.ProgrammingError:
            Log("FetchAll DataBase:\t%s" % e[1], True)
        else:
            Log("FetchAll:\t%s" % e, True)
        raise

def GetRefClientGroup(AgentID):
    return __FetchAll(SQL_CLIENT_GR, True)


def GetRefClient(AgentID):
    return __FetchAll(SQL_CLIENT, True, AgentID)


def GetRefProduct(AgentID):
    return __FetchAll(SQL_PRODUCT, True, AgentID)


def GetRefProductGroup(AgentID):
    return __FetchAll(SQL_PRODUCT_GR, True)


def GetRefTypeDoc(AgentID):
    return __FetchAll(SQL_TYPEDOC, True)


def __call_OrdersOutInv(id, Doc, DocDet, BpOptions):
    if DEBUG_LEVEL > 3:
        Log("OrdersOutInv:\tBeePresLinkID=%d" % id)

    orders_id = __FetchAll('select id from ordersoutinv where beepreslinkid = ?', False, id)
    if orders_id:
        orders_id = orders_id[0][0]
        __ExecuteStatement("update ordersoutinv set comment1 = 'OLD BeepresLinkID: '||beepreslinkid, beepreslinkid = null where id = ?", orders_id)

    params = {
        'DOCDATE':          Doc['docdate'],
        'CLIENTID':         Doc['client_id'],
        'STOREID':          BpOptions[0][0],
        'BEEPRESLINKID':    id,
        'TERMDATE':         Doc['paymentdate'],
        'PAYTYPEID':        Doc['paytype'],
        #'PRICETYPEID':      BpOptions[0][2],
        'AGENTID':          Doc['agent_id'],
        'OK_PASSED':        0}
    try:
        params['COMMENT2'] = Doc['description'][:35]
    except:
        pass

    orders_id = __ExecuteProcedure('EPRORDERSOUTINV_INSERT', params)[0]
    if DEBUG_LEVEL > 4:
        Log("OrdersOutInv:\tNew record ID=%d" % orders_id)

    for Det in DocDet:
        params = {'ORDERSOUTINVID': orders_id,  
                  'GOODSID':        Det['product_id'],
                  'ITEMCOUNT':      Det['qty']}
        __ExecuteProcedure('EPRORDERSOUTINVDET_INSERT', params)

    return orders_id



def __call_OrdersInMigr(id, Doc, DocDet, BpOptions):
    if DEBUG_LEVEL > 3:
        Log("OrdersInMigr:\tBeePresLinkID=%d" % id)

    try:
        description = Doc['description']
    except:
        description = ''
    suffix = ''
    if Doc['paytype'] == 2:
        suffix = '--- '
    else:
        if Doc['paytype'] == 6:
            suffix = '+++ '
    description = suffix + description

    params = {
        'DOCDATE':          Doc['docdate'],
        'CLIENTID':         Doc['client_id'],
        'STOREIDSRC':       BpOptions[0][0],
        'STOREIDDST':       BpOptions[0][1],
        'BEEPRESLINKID':    id,
        'TERMDATE':         Doc['paymentdate'],
        'PAYTYPEID':        Doc['paytype'],
        'COMMENT2':         description[:35],
        #'PRICETYPEID':      BpOptions[0][2],
        'AGENTID':          Doc['agent_id'],
        'OK_PASSED':        0}

    orders_id = __FetchAll('select id from ordersinmigr where beepreslinkid = ?', False, id)
    if orders_id:
        orders_id = orders_id[0][0]
        __ExecuteStatement("update ordersinmigr set comment1 = 'OLD BeepresLinkID: '||beepreslinkid, beepreslinkid = null where id = ?", orders_id)

    orders_id = __ExecuteProcedure('EPRORDERSINMIGR_INSERT', params)[0]
    if DEBUG_LEVEL > 4:
        Log("OrdersInMigr:\tNew record ID=%d" % orders_id)

    for Det in DocDet:
        params = {'ORDERSINMIGRID': orders_id,  
                  'GOODSID':        Det['product_id'],
                  'ITEMCOUNT':      Det['qty']}
        __ExecuteProcedure('EPRORDERSINMIGRDET_INSERT', params)
    return orders_id


def SetDoc(Doc, DocDet):
    BpOptions = __FetchAll(SQL_AGENTSTORE, False, Doc['agent_id'])

    if not BpOptions:
        raise Exception(u'В центральной базе нет настроек для агента в BpOptions')

    uniqueDocID = int("%d%d" % (Doc['agent_id'], Doc['_id']))

    try:
        if Doc['presventype'] == 0:
            # Заявки на НР
            if Doc['paytype1or2'] == 1:
                orders_id = __call_OrdersOutInv(uniqueDocID, Doc, DocDet, BpOptions)
            # Заявки на ВП
            if Doc['paytype1or2'] == 2:
                orders_id = __call_OrdersInMigr(uniqueDocID, Doc, DocDet, BpOptions)
        else:
            orders_id = 0
#            if Doc['paytype1or2'] == 1:
#                ProcParams = ()
#            else:
#                ProcParams = ()
        con.commit(True)
        return orders_id
    except Exception as e:
        con.rollback(True)
        if type(e) == fb.ProgrammingError:
            Log("SetDoc DataBase:\t%s" % e[1], True)
        else:
            Log("SetDoc:\t%s" % e, True)
        if DEBUG_LEVEL > 4:
            traceback.print_exc();
        raise
    
def SetLocation(AgentID, location):
    for rec in location:
        __ExecuteStatement(SQL_LOCATION, rec['date_location'], rec['provider'],
                           rec['lat'], rec['lon'], rec['accuracy'], int(AgentID))
    
    return True
