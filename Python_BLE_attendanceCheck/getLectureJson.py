#!/usr/bin/python3
# -*- coding: utf-8 -*-
print("Content-type:application/json;charset=utf-8\r\n")

#######################################################

import sys
import codecs
import cgi
import cgitb
import json

sys.stdout = codecs.getwriter("utf-8") (sys.stdout.detach())
cgitb.enable()

time_json = {"dotw":6, "class_time":20, "name":"컴퓨터구조", "code":"CSE316-01", "classroom": "S06-0611", "professr_number":"1234567"}
#구현의 편리함, 작동 확인의 편리함 위하여 고정값
time_json = json.dumps(time_json,indent=4, ensure_ascii=False)
print(time_json)
