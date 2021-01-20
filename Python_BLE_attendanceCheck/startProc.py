#!/usr/bin/python3
# -*- coding: utf-8 -*-
print("Content-type:text/html;charset=utf-8\r\n")

#######################################################

import sys
import codecs
import cgi
import cgitb
import requests
import json
import pymysql

sys.stdout = codecs.getwriter("utf-8") (sys.stdout.detach())
cgitb.enable()

form = cgi.FieldStorage()
cur = db.cursor(pymysql.cursors.DictCursor)
code = form['code'].value
classroom = form['classroom'].value

url = "https://fcm.googleapis.com/fcm/send"
get_member_sql = f"SELECT token FROM member WHERE timetable LIKE '%{code}%'"
cur.execute(get_member_sql)
rows = cur.fetchall()
uuid = ""
token = ""

for row in rows:
	token = row['token']
	get_UUID_sql = f"SELECT UUID FROM uuid_list WHERE classroom = '{classroom}'"
	cur.execute(get_UUID_sql)
	UUID_rows = cur.fetchall()
	
	for uuidrow in UUID_rows:
		uuid = uuidrow['UUID']
		break

	header = {
		'Content-type' : 'application/json',
		'Authorization' : 'key='
	}
	data = {
		"title" : "출석체크 시작알림",
		"message" : "출석체크를 시작합니다",
		"clickAction" : "bleCheck",
		"UUID" : uuid,
		"Code" : code
	}

	post_data = {
		'to' : token,
		'priority' : "high",
		'data' : data
	}

	session = requests.session()
	response = session.post(url, headers=header, data=json.dumps(post_data))
	print(response.text)


db.close()
