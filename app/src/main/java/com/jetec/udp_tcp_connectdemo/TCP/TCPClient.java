package com.jetec.udp_tcp_connectdemo.TCP;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

class TCPClient implements Runnable {
    private String TAG = TCPServer.TAG;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private String  serverIP;
    private int serverPort;
    private boolean isRun = true;
    private Socket socket;
    private Context context;



    public TCPClient(String ip , int port,Context context){
        this.serverIP = ip;
        this.serverPort = port;
        this.context = context;
    }
    public boolean getStatus(){
        return isRun;
    }

    public void closeClient(){
        isRun = false;
    }

    public void send(byte[] msg){
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(msg);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg){
        pw.print(msg);
        pw.flush();
    }

    @Override
    public void run() {
        byte[] buff = new byte[100];
        try {
            /**將Socket指向指定的IP & Port*/
            socket = new Socket(serverIP,serverPort);
            socket.setSoTimeout(5000);
            pw = new PrintWriter(socket.getOutputStream(),true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (isRun){
            try {
                int rcvLen = dis.read(buff);
                String rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                Log.d(TAG, "收到訊息: "+ rcvMsg);

                Intent intent =new Intent();
                intent.setAction(TCPServer.RECEIVE_ACTION);
                intent.putExtra(TCPServer.RECEIVE_STRING, rcvMsg);
                context.sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            pw.close();
            is.close();
            dis.close();
            socket.close();
            Log.d(TAG, "關閉Client");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
