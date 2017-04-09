package com.example.xiaxia.demo1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

/**
 * Created by Xiaxia on 2017/4/5.
 */

public class TestActivity extends Activity {
    EditText editposx,editposy;
    TextView textviewposx,textviewposy;
    Button btn_one,btn_done;
    ArrayList<String> aps;
    ArrayList<Point> points;
    List<ScanResult> list;
    private WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        aps=new ArrayList<>();
        points=new ArrayList<>();
        wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        editposx=(EditText)findViewById(R.id.pos_x);
        editposy=(EditText)findViewById(R.id.pos_y);
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
                        Point p=new Point(Double.parseDouble(editposx.getText().toString()),Double.parseDouble(editposy.getText().toString()));
                        if(!points.contains(p)){
                            points.add(p);
                        }
                        if(!wifiManager.isWifiEnabled()){
                            wifiManager.setWifiEnabled(true);
                        }
                        String rssvalue="insert into Rss values ";
                        for(int i=0;i<64;i++) {
                            String s="('";
                            s=s+p.getX()+"','"+p.getY()+"','";
                            list = wifiManager.getScanResults();
                            for(int j=0;j<list.size();j++){
                                ScanResult scanResult=list.get(j);
                                if(!aps.contains(scanResult.BSSID)){
                                    aps.add(scanResult.BSSID);
                                }
                                s=s+scanResult.BSSID+"="+scanResult.level+";";
                            }
                            s=s+"')";
                            rssvalue=rssvalue+s+(i==63?';':',');
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
