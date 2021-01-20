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
code = form['code'].value;
number = form['number'].value;
major = form['major'].value;
minor = form['minor'].value;

code_split = (code.split("-"))[0]
get_className_sql = f"SELECT class_name FROM code_to_name WHERE class_code = '{code_split}'"
cur.execute(get_className_sql)
namerows = cur.fetchall()

for nrow in namerows:
	class_name = nrow['class_name']
	break

get_code_sql = f"SELECT * FROM major_minor WHERE code = '{code}' ORDER BY time DESC limit 1"
cur.execute(get_code_sql)
rows = cur.fetchall()

for row in rows:
	if int(row['major']) == int(major) and int(row['minor']) == int(minor):
		get_identity_sql = f"SELECT identity FROM member WHERE number = {number}"
		cur.execute(get_identity_sql)
		identity_rows = cur.fetchall()
		for irow in identity_rows:
			check_sql = f"INSERT INTO check_list(identity, number, class_name, code, major, minor, time) VALUES({irow['identity']},{number},'{class_name}','{code}',{major},{minor},now())"
			cur.execute(check_sql)
			db.commit()
db.commit()
db.close()