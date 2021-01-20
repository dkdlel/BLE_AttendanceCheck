#!/usr/bin/python3
# -*- coding: utf-8 -*-
print("Content-type:text/html;charset=utf-8\r\n")

#######################################################

import sys
import codecs
import cgi
import cgitb
import pymysql

sys.stdout = codecs.getwriter("utf-8") (sys.stdout.detach())
cgitb.enable()

form = cgi.FieldStorage()
cur = db.cursor(pymysql.cursors.DictCursor)
code = form['code'].value
major = int(form['major'].value,16)
minor = int(form['minor'].value,16)

sql = f"INSERT major_minor VALUES('{code}',{major},{minor},now())"
cur.execute(sql)
db.commit()
db.close()
