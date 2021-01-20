package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn1 = (Button)findViewById(R.id.btn1);
        Button save_btn = (Button)findViewById(R.id.save_btn);
        btn1.setBackgroundColor(Color.parseColor("#0080FF"));
        save_btn.setBackgroundColor(Color.parseColor("#0080FF"));
        TextView id = (TextView)findViewById(R.id.id_text);
        mContext = this;
        id.setText(PreferenceManager.getString(mContext, "student_id"));
        save_btn.setOnClickListener(new View.OnClickListener() { //로그인 대체
            @Override
            public void onClick(View v) {
                String input = id.getText().toString();
                if(!input.equals(""))
                {
                    PreferenceManager.setString(mContext, "student_id", input);
                    Toast.makeText(getApplicationContext(),"학번이 저장되었습니다!",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "학번을 입력해주세요!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, checkList.class);
                startActivity(intent);
            }
        });
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String student_number = PreferenceManager.getString(mContext, "student_id");
        if(!student_number.equals(""))
        {
            ContentValues values = new ContentValues();
            values.put("number", student_number);
            values.put("token", refreshedToken);
            bleCheck.NetworkTask networkTask = new bleCheck.NetworkTask("서버주소/setMemberToken.py", values);//사용자 토큰값을 앱 실행시 서버에 보내어 데이터베이스에 저장 -> 푸시알림시 사용
            networkTask.execute();
        }
    }



}