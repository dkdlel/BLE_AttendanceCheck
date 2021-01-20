package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class checkList extends AppCompatActivity
{
    Button btn1;
    TextView loadingtv;
    ListView lv1;
    ArrayAdapter<String> itemAdapter;
    String spinner_string = "";
    JSONObject json = null;
    JSONArray jsonArr;
    String flag;
    LinearLayout bt_line, bt_line_V;
    int code_flag = 0, list_flag = 0;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);
        btn1 = (Button)findViewById(R.id.btn1);
        bt_line = (LinearLayout)findViewById(R.id.bt_line);
        bt_line_V = (LinearLayout)findViewById(R.id.bt_line_V);
        btn1.setBackgroundColor(Color.parseColor("#0080FF"));
        lv1 = (ListView)findViewById(R.id.lv);
        loadingtv = (TextView)findViewById(R.id.loadingtv);
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        // 학번
        mContext = this;
        String student_number = PreferenceManager.getString(mContext, "student_id");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                flag = (String)parent.getItemAtPosition(position);
                lv1.setVisibility(View.GONE);
                ContentValues flag_values = new ContentValues();
                flag_values.put("number",student_number);
                flag_values.put("name",flag);
                NetworkTask flag_networkTask = new NetworkTask("서버주소/getClassCode.py",flag_values); //강의이름을 보내면 해당 학생의 과목번호(분반 포함)를 반환
                flag_networkTask.execute();
                code_flag = 1;
                if(flag_networkTask.getStatus() == AsyncTask.Status.RUNNING)
                {
                    loadingtv.setVisibility(View.VISIBLE);
                    bt_line.setVisibility(View.GONE);
                    bt_line_V.setVisibility(View.VISIBLE);
                    loadingtv.setText("로딩중");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(),"과목을 선택하지 않았습니다!",Toast.LENGTH_SHORT).show();
            }
        });
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loadingtv.getText().equals("로딩중"))
                {
                    Toast.makeText(getApplicationContext(),"로딩중입니다!",Toast.LENGTH_SHORT).show();
                }
                else
                    {
                    if (!spinner_string.equals("")) {
                        lv1.setVisibility(View.GONE);
                        ContentValues values = new ContentValues();
                        values.put("number", student_number);
                        values.put("code", spinner_string);
                        NetworkTask networkTask = new NetworkTask("서버주소/getCheckList.py", values); //학번, 과목번호(분반 포함)을 보내면 해당 학생의 출결현황을 반환
                        networkTask.execute();
                        list_flag = 1;
                        if (networkTask.getStatus() == AsyncTask.Status.RUNNING) {
                            loadingtv.setVisibility(View.VISIBLE);
                            bt_line.setVisibility(View.GONE);
                            bt_line_V.setVisibility(View.VISIBLE);
                            loadingtv.setText("로딩중");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "과목을 선택하지 않았습니다!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        String url;
        ContentValues values;

        public NetworkTask(String url, ContentValues values){
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            requestHttpURLConnection requestHttpURLConnection = new requestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadingtv.setText("");
            loadingtv.setVisibility(View.GONE);
            if(result != null)
            {
                try {
                    json = new JSONObject(result);
                    jsonArr = json.getJSONArray("result");
                    if(code_flag == 1) //과목번호 얻는 분기
                    {
                        try {
                            json = jsonArr.getJSONObject(0);
                            spinner_string = json.getString("code"); //강의이름을 보내 받은 과목번호(분반포함)를 저장
                            code_flag = 0;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        bt_line.setVisibility(View.VISIBLE);
                        bt_line_V.setVisibility(View.GONE);
                    }
                    else if(list_flag == 1) //해당 학생의 출결현황을 얻는 분기
                    {
                        ArrayList<String> list = new ArrayList<String>();
                        int i;
                        for(i=0; i<jsonArr.length(); i++)
                        {
                            try {
                                json = jsonArr.getJSONObject(i);
                                list.add(json.getString("class_name") + "\n" + json.getString("code")+"\n"+json.getString("time")); //학번, 과목번호를 보내 받은 강의이름, 강의번호, 출석시간을 리스트에 추가
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        itemAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_type, list);
                        lv1.setAdapter(itemAdapter);
                        lv1.setVisibility(View.VISIBLE);
                        bt_line.setVisibility(View.GONE);
                        bt_line_V.setVisibility(View.VISIBLE);
                        list_flag = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }
    }


}