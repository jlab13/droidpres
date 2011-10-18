#!/usr/bin/env python
#-*- coding: utf-8 -*-"

import os
from subprocess import PIPE, Popen

# Уровень отладки
DEBUG_LEVEL = 5

# Параметры HTTP сервера для XML-RPC запросов
SERVER_ADRES    = '0.0.0.0'
SERVER_PORT     = 8888
SERVER_RPC_PATH = '/droidpressrpc2'
SERVER_USER     = 'DroidPres'
SERVER_PASSWD   = 'ohjaTho5gah2Ohyoh5Ug'

# Модуль посредник для обменна данными
PROXY_MODULE    = 'proxy_trukr_1c'
#PROXY_MODULE    = 'proxy_demo_1c'
#PROXY_MODULE    = 'proxy_demo_sqlite'
#PROXY_MODULE    = 'proxy_ctrader'

WORK_DIR        = os.path.dirname(__file__).replace('\\','/') + '/'
APK_FILENAME    = WORK_DIR + 'apk/DroidPres.apk'

def GetApkVersion():
    CMD = [WORK_DIR + 'apk/aapt', 'dump', 'badging', APK_FILENAME]
    p = Popen(CMD, shell=False, stdout=PIPE)
    stdout_value = p.communicate()[0]
    return int(stdout_value.split()[2].split('=')[1].replace("'",""))
