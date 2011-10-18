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


from time import localtime, strftime
from sys import stdout, stderr

def Log(str, err_flag=False):
    if err_flag:
        line = "[%s]\tERROR: %s" % (strftime("%d.%m.%Y %H:%M:%S", localtime()), ' '.join(str.__str__().split()))
    else:
        line = "[%s]\t%s" % (strftime("%d.%m.%Y %H:%M:%S", localtime()), ' '.join(str.__str__().split()))

    line = line.replace('\\n', '')

    if err_flag:
        print >> stderr, line
        stderr.flush()
    else:
        print >> stdout, line
        stdout.flush()
