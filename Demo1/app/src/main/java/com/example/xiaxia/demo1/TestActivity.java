package com.example.xiaxia.demo1;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;



/**
 * Created by Xiaxia on 2017/4/5.
 */

public class TestActivity extends Activity {
//    EditText editposx,editposy;
    TextView textviewposx,textviewposy;
    Button btn_one,btn_done;
    ArrayList<String> aps;
    ArrayList<Point> points;
    ArrayList<String> postion;
    List<ScanResult> list;
    NumberPicker numberPicker1,numberPicker2;
    private WifiManager wifiManager;
    ListView listView,listView1;
    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter1;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        aps=new ArrayList<>();
        postion=new ArrayList<>();


        listView=(ListView)findViewById(R.id.listview) ;
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,aps);
        listView.setAdapter(adapter);

        listView1=(ListView)findViewById(R.id.listview1);
        adapter1=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,postion);
        listView1.setAdapter(adapter1);

        final Handler handler=new Handler() {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 4:
                        adapter.notifyDataSetChanged();
                        break;
                    case 5:
                        Toast.makeText(TestActivity.this,"离线采集一次完成",Toast.LENGTH_LONG).show();
                        break;
                    case 6:
                        adapter1.notifyDataSetChanged();
                        break;
                }

            }
        };

        String[] values=new String[]{
               "0.0","0.5","1.0","1.5","2.0","2.5","3.0","3.5","4.0","4.5","5.0","5.5","6.0","6.5","7.0","7.5","8.0","8.5","9.0","9.5",
                "10.0","10.5","11.0","11.5","12.0","12.5","13.0","13.5","14.0","14.5","15.0","15.5","16.0","16.5","17.0","17.5","18.0","18.5","19.0","19.5",
                "20.0","20.5","21.0","21.5","22.0","22.5","23.0","23.5","24.0","24.5","25.0","25.5","26.0","26.5","27.0","27.5","28.0","28.5","29.0","29.5",
        };
        numberPicker1=(NumberPicker)findViewById(R.id.np1);
        numberPicker2=(NumberPicker)findViewById(R.id.np2);
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(values.length-1);
        numberPicker1.setDisplayedValues(values);
        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(values.length-1);
        numberPicker2.setDisplayedValues(values);

        points=new ArrayList<>();
        wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
//        editposx=(EditText)findViewById(R.id.pos_x);
//        editposy=(EditText)findViewById(R.id.pos_y);
        textviewposx=(TextView)findViewById(R.id.posx);
        textviewposy=(TextView)findViewById(R.id.posy);
        btn_one=(Button)findViewById(R.id.getone);
        btn_done=(Button)findViewById(R.id.done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        Looper.prepare();
                        String doaps="insert into aps values ";
                        for(int i=0;i<aps.size();i++){
                            String ss="('"+aps.get(i)+"')";
                            doaps+=ss;
                            doaps+=(i==(aps.size()-1)?';':',');
                        }
                        String dopos="insert into postion values ";
                        for(int j=0;j<points.size();j++){
                            String s2="('"+points.get(j).getX()+"','"+points.get(j).getY()+"')";
                            dopos+=s2;
                            dopos+=(j==(points.size()-1)?';':',');
                        }
                        try{
                            URL url=new URL("http://192.168.1.100:8080/serverlet/MyServlet");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            connection.setRequestProperty("charset", "UTF-8");
                            connection.setDoOutput(true);
                            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                            out.writeBytes(doaps);
                            out.writeBytes("\n");
                            out.writeBytes(dopos);
                            if (connection.getResponseCode()==200){
                                InputStream in = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                StringBuilder s = new StringBuilder();
                                String line;
                                //读取服务器返回的数据
                                while ((line = reader.readLine()) != null) {
                                    s.append(line);
                                }
                                reader.close();
                            }
                            out.close();
                        }catch (MalformedURLException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        Intent intent=new Intent(TestActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }).start();

            }
        });
        btn_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获取点位置信息
                        //Looper.prepare();
                        String rssvalue = "insert into Rss values ";
                        try {
                            //wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                            //Point p = new Point(Double.parseDouble(editposx.getText().toString()), Double.parseDouble(editposy.getText().toString()));
                            Point p = new Point(numberPicker1.getValue()*0.5,numberPicker2.getValue()*0.5);
                            if (!points.contains(p)) {
                                points.add(p);
                                String s_p="("+p.getX()+","+p.getY()+")";
                                postion.add(s_p);
                            }
                            if (!wifiManager.isWifiEnabled()) {
                                wifiManager.setWifiEnabled(true);
                            }
                            //String rssvalue = "insert into Rss values ";
                            for (int i = 0; i < 64; i++) {
                                String s = "('";
                                s = s + p.getX() + "','" + p.getY() + "','";
                               // wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                                wifiManager.startScan();
                                list = wifiManager.getScanResults();
                                for (int j = 0; j < list.size(); j++) {
                                    ScanResult scanResult = list.get(j);
                                    if (!aps.contains(scanResult.BSSID)) {
                                        aps.add(scanResult.BSSID);
//                                        Message message=new Message();
//                                        message.what=4;
//                                        handler.sendMessage(message);
                                    }
                                    s = s + scanResult.BSSID + "=" + scanResult.level + ";";
                                }
                                s = s + "')";
                                rssvalue = rssvalue + s + (i == 63 ? ';' : ',');
                                Thread.sleep(200);
                            }
                            //Toast.makeText(TestActivity.this,"离线采集一次完成",Toast.LENGTH_LONG).show();
                            Message message5=new Message();
                            message5.what=5;
                            handler.sendMessage(message5);
                            //aps
                            Message message4=new Message();
                            message4.what=4;
                            handler.sendMessage(message4);
                            //postion
                            Message message6=new Message();
                            message6.what=6;
                            handler.sendMessage(message6);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        try{
                            URL url=new URL("http://192.168.1.100:8080/serverlet/MyServlet");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setConnectTimeout(5000);
                            connection.setReadTimeout(5000);
                            connection.setRequestProperty("charset", "UTF-8");
                            connection.setDoOutput(true);
                            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                            out.writeBytes(rssvalue);
                            if (connection.getResponseCode()==200){
                                InputStream in = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                StringBuilder s = new StringBuilder();
                                String line;
                                //读取服务器返回的数据
                                while ((line = reader.readLine()) != null) {
                                    s.append(line);
                                }
                                reader.close();
                            }
                            out.close();
                        }catch (MalformedURLException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }
}
