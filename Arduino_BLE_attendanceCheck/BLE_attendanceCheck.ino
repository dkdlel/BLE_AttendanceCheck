#include <ArduinoJson.h>
#include <Ethernet.h>
#include <SPI.h>
#include <SoftwareSerial.h>
#include <SoftReset.h>

SoftwareSerial BTSerial(3,4);
unsigned long previousMillis = 0;
const long delayTime = 1000;
const long resetdelayTime = 90000; //90초간격으로 출석 과정 반복
int Major;
int Minor;
String Majors;
String Minors;
String classroom;
String code;

String Command[] = {"AT+RENEW", "AT+RESET", "AT", "AT+MARJ0x", "AT+MINO0x", "AT+ADVI5", "AT+NAMEBCHK", "AT+ADTY3", "AT+IBEA1", "AT+DELO2", "AT+PWRM0"};

void send_command(String cmd) //AT Command를 BTSerial.write로 입력
{
  for(int i=0; i<cmd.length(); i++)
  {
    BTSerial.write(cmd[i]);
  }
  
}

void setup() 
{  
  Serial.begin(9600);
  BTSerial.begin(9600);
  while (!Serial) continue;

  byte mac[] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};
  if (!Ethernet.begin(mac)) {
    Serial.println(F("Failed to configure Ethernet"));
    return;
  }
  delay(1000);

  Serial.println(F("Connecting..."));

  EthernetClient client;
  client.setTimeout(10000);
  if (!client.connect("서버주소", 80)) {
    Serial.println(F("Connection failed"));
    return;
  }
  Serial.println(F("Connected!"));


  client.println(F("GET /getLectureJson.py HTTP/1.0")); //getLectureJson.py에서 비콘이 위치한 강의실의 강의정보 출력
  client.println(F("Host: 서버주소"));
  client.println(F("Connection: close"));
  if (client.println() == 0) {
    Serial.println(F("Failed to send request"));
    return;
  }

  char status[32] = {0};
  client.readBytesUntil('\r', status, sizeof(status));
  if (strcmp(status, "HTTP/1.1 200 OK") != 0) {
    Serial.print(F("Unexpected response: "));
    Serial.println(status);
    return;
  }

  char endOfHeaders[] = "\r\n\r\n";
  if (!client.find(endOfHeaders)) {
    Serial.println(F("Invalid response"));
    return;
  }

  const size_t capacity = JSON_OBJECT_SIZE(6) + 100;
  DynamicJsonDocument doc(capacity);

  DeserializationError error = deserializeJson(doc, client);
  if (error) {
    Serial.print(F("deserializeJson() failed: "));
    Serial.println(error.f_str());
    return;
  }
  //받아온 강의정보 JSON
  Serial.println(F("Response:"));
  Serial.println(doc["dotw"].as<int>()); //요일
  Serial.println(doc["class_time"].as<int>()); //강의시간
  Serial.println(doc["name"].as<char*>()); //강의명
  Serial.println(doc["code"].as<char*>()); //과목번호
  Serial.println(doc["classroom"].as<char*>()); //강의실
  Serial.println(doc["professor_number"].as<char*>()); //강의담당 교수 교번
  
  //전송에 사용할 JSON 값들만 Stringfy
  String code_temp = doc["code"];
  code = code_temp;
  String classroom_temp = doc["classroom"];
  classroom = classroom_temp;
  client.stop();

  
  randomSeed(analogRead(0));
  Major = random(2,50000); //Major값 난수 생성
  randomSeed(analogRead(0));
  Minor = random(3,50000); //Minor값 난수 생성
  Majors = String(Major, HEX); //Major값 16진수화
  Minors = String(Minor, HEX); //Minor값 16진수화

  client.setTimeout(10000);
  if (!client.connect("서버주소", 80)) {
    Serial.println(F("Connection failed"));
    return;
  }
  Serial.println(F("Connected!"));

  client.print(F("GET /setProc.py")); //Major, Minor, 과목번호 전송 -> setProc.py에서 디비에 저장.
  client.print(("?major=" + Majors));
  client.print(("&minor=" + Minors));
  client.print(("&code=" + code));
  client.println(F(" HTTP/1.0"));
  client.println(F("Host: 서버주소"));
  client.println(F("Connection: close"));
  if (client.println() == 0) {
    Serial.println(F("Failed to send request"));
    return;
  }

 client.readBytesUntil('\r', status, sizeof(status));
  if (strcmp(status, "HTTP/1.1 200 OK") != 0) {
    Serial.print(F("Unexpected response: "));
    Serial.println(status);
    return;
  }
}

int i = 0;
unsigned long startMillis = millis(); //시작시간
void loop() {
  unsigned long currentMillis = millis(); //현재시간
  if(currentMillis - startMillis >= resetdelayTime)
  {
    Serial.println("Restart For Next Check"); //출결 과정 반복을 위한 아두이노 재시작
    soft_restart();
  }
  if(i < 11)
  {
    if(currentMillis - previousMillis >= delayTime) //1초가 지났는지 확인
    {
      previousMillis = currentMillis; //1초 지나면 이전시간 갱신
      if(i == 3) //Major값 난수 이어붙이기
      {
        Majors.toUpperCase();
        send_command(Command[i++]+Majors);
      }
      else if(i == 4) //Minor값 난수 이어붙이기
      {
        Minors.toUpperCase();
        send_command(Command[i++]+Minors);
      }
      else
      {
        send_command(Command[i++]);  
      }
      Serial.println();
    }  
  }
  if(i == 11)
  {
    EthernetClient client;
    client.setTimeout(10000);
    if (!client.connect("서버주소", 80)) 
    {
      Serial.println(F("Connection failed"));
      return;
    }

    Serial.println(F("Connected!"));

    client.print(F("GET /startProc.py")); //과목번호와 강의실 전송 -> startProc.py에서 해당 강의실의 해당 과목 수강생들에게 푸시알림 보내어 출석 시작
    client.print(("?classroom=" + classroom));
    client.print(("&code=" + code));
    client.println(F(" HTTP/1.0"));
    client.println(F("Host: 서버주소"));
    client.println(F("Connection: close"));
    if (client.println() == 0) 
    {
      Serial.println(F("Failed to send request"));
      return;
    }

    char status[32] = {0};
    client.readBytesUntil('\r', status, sizeof(status));
    if (strcmp(status, "HTTP/1.1 200 OK") != 0) 
    {
      Serial.print(F("Unexpected response: "));
      Serial.println(status);
      return;
    }
    i++; //조건문에 반복하여 진입하지 않도록 i값 1 증가
  }
  
  while (Serial.available()){
    byte data = Serial.read();
    BTSerial.write(data); 
  }
  
  while (BTSerial.available()){ 
    byte data = BTSerial.read();
    Serial.write(data);
  }  
  
}
