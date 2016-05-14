package com.dikaros.filetransfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.dikaros.filetransfer.socket.FileTransferAsyncTask;
import com.dikaros.filetransfer.util.AlertUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    //wifi p2p管理器
    private WifiP2pManager mManager;

    //wifi p2p通道
    private WifiP2pManager.Channel mChannel;
    //意图过滤器
    IntentFilter mIntentFilter;
    //广播接收器
    WiFiDirectBroadcastReceiver mReceiver;

    ListView lvMain;
    Button btnMain;
    ArrayAdapter<WifiP2pDevice>  adapter;


    //connection
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverBinder = (SocketServerService.SocketBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //socket binder
    SocketServerService.SocketBinder serverBinder;


    ProgressBar pBarMain;

    boolean isSender = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        //
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        initViews();

        //启动服务通过bind方式
        Intent service = new Intent(MainActivity.this,SocketServerService.class);
        //绑定服务
        bindService(service,connection,BIND_AUTO_CREATE);

        //主机收到请求
        serverBinder.setOnClientConnectedListener(new SocketServerService.OnClientConnectedListener() {
            @Override
            public void OnClientConnected(Socket socket) {
                //do sendFile or receiveFile
                //如果是接收方
                if (!Config.isSenerDevice){
                    try {
                        //启动接收器
                        serverBinder.reveiceFile(MainActivity.this,socket,Config.getPersonalFile());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        //发送文件
                        serverBinder.sendFile(MainActivity.this,socket,Config.getPersonalFile());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * 连接到owner
     */
    public void connectToOwner(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Socket clientSocket;
                    clientSocket = new Socket();
                    clientSocket.bind(null);
                    clientSocket.connect(new InetSocketAddress(Config.CONNECTED_OWNER_IP, 8888), 3000);
                    serverConnected(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 连接上了服务器
     * @param socket
     */
    private void serverConnected(Socket socket){
        FileTransferAsyncTask transferAsyncTask = new FileTransferAsyncTask(this,socket,Config.CONNECTED_OWNER_IP);
        if (isSender){
//            transferAsyncTask.setSendFileListile(new File(""));

            //设置并发送文件
        }
        transferAsyncTask.execute(isSender);
    }





    /**
     * 初始化Views
     */
    private void initViews() {
        lvMain = (ListView) findViewById(R.id.lv_main);
        btnMain = (Button) findViewById(R.id.btn_search);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        lvMain.setAdapter(adapter);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                pBarMain.setVisibility(View.VISIBLE);
                v.setEnabled(false);
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        AlertUtil.toastMess(MainActivity.this,"加载成功");
                    }

                    @Override
                    public void onFailure(int reason) {
                        AlertUtil.toastMess(MainActivity.this,"加载失败"+reason);
                        pBarMain.setVisibility(View.GONE);
                        v.setEnabled(true);
                    }
                });
            }
        });
        pBarMain = (ProgressBar) findViewById(R.id.pBar_main);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice device =  adapter.getItem(position);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        AlertUtil.toastMess(MainActivity.this,"连接成功");
                    }

                    @Override
                    public void onFailure(int reason) {
                        AlertUtil.toastMess(MainActivity.this,"连接成功"+reason);
                    }
                });
            }
        });

    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        //解绑服务
        unbindService(connection);
        super.onDestroy();

    }


}
