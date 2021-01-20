package com.example.myapplication;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class bleCheck extends AppCompatActivity implements BeaconConsumer, AutoPermissionsListener {
    TextView textView;
    Intent tmp;

    private Context mContext;
    private String student_number;
    private BeaconManager beaconManager;
    public String beaconUUID, Code;
    public int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blecheck);
        mContext = this;
        student_number = PreferenceManager.getString(mContext, "student_id");
        tmp = getIntent();
        beaconUUID = tmp.getStringExtra("UUID");
        Code = tmp.getStringExtra("Code");
        textView=findViewById(R.id.tv_message);
        AutoPermissions.Companion.loadAllPermissions(this,101);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

    }

    @Override
    public void onBeaconServiceConnect() { beaconManager.removeAllMonitorNotifiers();
        beaconManager.setRangeNotifier(new RangeNotifier()
        {
            @Override
            public void didRangeBeaconsInRegion(Collection beacons, Region region)
            {
                try{
                    Thread.sleep(1000);
                    cnt++;
                }catch (Exception e){}
                if (beacons.size() > 0)
                {
                    String url = "서버주소/getToken.py"; //학번, 과목코드, Major, Minor값을 보내 전송 -> getToken.py에서 서버 내 데이터베이스에 저장된 Major, Minor값과 비교하여 일치하면 해당 학번 출석체크
                    ContentValues value = new ContentValues();
                    value.put("number", student_number); //학번
                    value.put("code",Code); //과목코드 받아와서 같이 보내줌
                    value.put("major", (((Beacon)beacons.iterator().next()).getId2()).toString()); //비콘 검색결과로 나온 Major값
                    value.put("minor", ((Beacon)beacons.iterator().next()).getId3().toString()); //비콘 검색결과로 나온 Minor값
                    NetworkTask networkTask = new NetworkTask(url, value);
                    networkTask.execute();

                    try{
                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(9999);
                        beaconManager.stopMonitoringBeaconsInRegion(region);
                        beaconManager.stopRangingBeaconsInRegion(region);
                        Thread.sleep(2000);
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                    catch (Exception e){

                    }
                }
                else if(cnt == 30){ //30초간 검색 실패시 종료 정차
                    cnt = 0;
                    try {
                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(9999);
                        beaconManager.stopMonitoringBeaconsInRegion(region);
                        beaconManager.stopRangingBeaconsInRegion(region);
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }catch (Exception e){}
                }
            }
        });
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Toast.makeText(bleCheck.this, "출석체크 진행중", Toast.LENGTH_SHORT).show();
                textView.setText("Beacon connected");

            }

            @Override
            public void didExitRegion(Region region) {
                Toast.makeText(bleCheck.this, "비콘 연결 끊김", Toast.LENGTH_SHORT).show();
                textView.setText("Beacon disconnected");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
            }

        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("beacon", Identifier.parse(beaconUUID), null, null));
        } catch (RemoteException e) {    }
        try
        {
            beaconManager.startRangingBeaconsInRegion(new Region("beacon", Identifier.parse(beaconUUID), null, null));
        }
        catch (RemoteException e)
        {
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onDenied(int i, String[] strings) {
    }

    @Override
    public void onGranted(int i, String[] strings) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    public static class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
