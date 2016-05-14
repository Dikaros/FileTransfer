package com.dikaros.filetransfer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import com.dikaros.filetransfer.socket.FileTransferAsyncTask;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServerService extends Service {
    public SocketServerService() {
    }


    //socketBinder
    SocketBinder serverBinder = new SocketBinder();

    //服务端socket
    ServerSocket serverSocket;




    //服务器线程
    Thread serverThread = new Thread() {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    serverSocket = new ServerSocket(8888);
                    Socket currentClientSocket = serverSocket.accept();
                    //callback when comes a connection
                    if (serverBinder.getOnClientConnectedListener()!=null){
                        serverBinder.getOnClientConnectedListener().OnClientConnected(currentClientSocket);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        serverThread.start();
        return serverBinder;
    }

    //套接字操作类
    class SocketBinder extends Binder {


        OnClientConnectedListener onClientConnectedListener = null;

        public void setOnClientConnectedListener(OnClientConnectedListener onClientConnectedListener) {
            this.onClientConnectedListener = onClientConnectedListener;
        }

        public OnClientConnectedListener getOnClientConnectedListener() {
            return onClientConnectedListener;
        }

        /**
         * 启动sercerTask
         *
         * @param context
         * @param file
         * @param isSender
         * @throws Exception
         */
        public FileTransferAsyncTask startServer(Context context, Socket socket, File file, boolean isSender) throws Exception {
            if (file == null) {
                throw new Exception("file can't be null");
            } else if (Config.CONNECTED_OWNER_IP == null) {
                throw new Exception("Can't get owner ip, please check wifi p2p connection");
            }
            FileTransferAsyncTask serverTask = new FileTransferAsyncTask(context, socket, Config.CONNECTED_OWNER_IP);
            serverTask.execute(file,isSender);
            return serverTask;
        }

        /**
         * 发送文件
         * @param context
         * @param file
         * @throws Exception
         */
        public void sendFile(Context context,Socket socket,File file) throws Exception {
            startServer(context,socket,file,true);

        }

        /**
         * 接收文件
         * @param context
         * @param file
         * @throws Exception
         */
        public void reveiceFile(Context context,Socket socket,File file) throws Exception {
            startServer(context,socket,file,false);

        }





    }

    interface  OnClientConnectedListener{
        /**
         * 有客户端连接到了服务
         */
        public void OnClientConnected( Socket socket);
    }


//    class SocketListener extends Thread{
//        Socket socket;
//        SocketListener(Socket socket){
//            this.socket = socket;
//        }
//
//        boolean running = true;
//
//        public boolean isRunning() {
//            return running;
//        }
//
//        public void setRunning(boolean running) {
//            this.running = running;
//        }
//
//        public void stopListener(){
//            running = false;
//        }
//
//        @Override
//        public void run() {
//            while (running){
//                try {
//                    int result = socket.getInputStream().read();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public static int SENDER = 0x2;
    public static int RECEIVER = 0x4;

    @Override
    public void unbindService(ServiceConnection conn) {
        try {
            //打断线程
            serverThread.interrupt();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        super.unbindService(conn);
    }
}
