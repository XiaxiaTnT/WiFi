package com.example.xiaxia.demo1;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
public class MainActivity extends AppCompatActivity {
    Button Test,Postion;
    ArrayList<String> onlineaps;
    ArrayList<Double> sumlist;
    ArrayList<Integer> timeslist;
    ArrayList<Double> averagelist;
    private WifiManager wifiManager;
    List<ScanResult> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Test=(Button)findViewById(R.id.button_test);
        Postion=(Button)findViewById(R.id.button_pos);
        Test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                        Intent intent=new Intent(MainActivity.this,TestActivity.class);
                        startActivity(intent);
            }
        });
        Postion.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获得rss信息，发送给服务器，获得返回值
                        Looper.prepare();
                        onlineaps=new ArrayList<>();
                        sumlist=new ArrayList<>();
                        timeslist=new ArrayList<>();
                        String res="";
                        wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
                        if(!wifiManager.isWifiEnabled()){
                            wifiManager.setWifiEnabled(true);
                        }
                        for(int i=0;i<64;i++) {
                            list = wifiManager.getScanResults();
                            for(int j=0;j<list.size();j++){
                                ScanResult scanResult=list.get(j);
                                if(onlineaps.contains(scanResult.BSSID)){
                                    sumlist.set(onlineaps.indexOf(scanResult.BSSID),sumlist.get(onlineaps.indexOf(scanResult.BSSID))+Double.valueOf(scanResult.level));
                                    timeslist.set(onlineaps.indexOf(scanResult.BSSID),timeslist.get(onlineaps.indexOf(scanResult.BSSID))+1);
                                }else if(!onlineaps.contains(scanResult.BSSID)){
                                    onlineaps.add(scanResult.BSSID);
                                    sumlist.add(Double.valueOf(scanResult.level));
                                    timeslist.add(1);
                                }
                            }
                        }
                        for (int i=0;i<onlineaps.size();i++){
                            res+=onlineaps.get(i);
                            res+="=";
                            res+=(sumlist.get(i)/(timeslist.get(i)*1.0));
                            res+=";";
                        }
                        HttpURLConnection connection;
                        try {
                            String Res= URLEncoder.encode(res,"UTF-8");
                            URL url = new URL("http://192.168.1.100:8080/serverlet/MyServlet?request=" + Res);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setReadTimeout(5000);
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("charset", "UTF-8");
                            connection.setConnectTimeout(5000);
                            connection.setDoInput(true);
                            //connection.setDoOutput(true);
                            if (connection.getResponseCode() == 200){
                                InputStream in = connection.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                String line;
                                StringBuilder s = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    Toast.makeText(getApplicationContext(),line,Toast.LENGTH_LONG).show();
                                    s.append(line);
                                }
                                reader.close();
                            }
                            Looper.loop();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
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
