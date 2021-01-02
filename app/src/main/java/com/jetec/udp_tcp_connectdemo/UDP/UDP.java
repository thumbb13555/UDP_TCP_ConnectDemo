package com.jetec.udp_tcp_connectdemo.UDP;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

class UDP implements Runnable {
    public static final String TAG = "MyUDP";
    public static final String RECEIVE_ACTION = "GetUDPReceive";
    public static final String RECEIVE_STRING = "ReceiveString";
    public static final String RECEIVE_BYTES = "ReceiveBytes";

    private int port = 8888;
    private String ServerIp;
    private boolean isOpen;
    private static DatagramSocket ds = null;
    private Context context;

    /**切換伺服器監聽狀態*/
    public void changeServerStatus(boolean isOpen) {
        this.isOpen = isOpen;
        if (!isOpen) {
            ds.close();
            Log.e(TAG, "UDP-Server已關閉");
        }
    }
    //切換Port
    public void setPort(int port){
        this.port = port;
    }
    /**初始化建構子*/
    public UDP(String ServerIp,Context context) {
        this.context = context;
        this.ServerIp = ServerIp;
        this.isOpen = true;

    }
    /**發送訊息*/
    public void send(String string, String remoteIp, int remotePort) throws IOException {
        Log.d(TAG, "客户端IP：" + remoteIp + ":" + remotePort);
        InetAddress inetAddress = InetAddress.getByName(remoteIp);
        DatagramSocket datagramSocket = new DatagramSocket();
        DatagramPacket dpSend = new DatagramPacket(string.getBytes(), string.getBytes().length, inetAddress, remotePort);
        datagramSocket.send(dpSend);

    }
    /**監聽執行緒*/
    @Override
    public void run() {
        /**在本機上開啟Server監聽*/
        InetSocketAddress inetSocketAddress = new InetSocketAddress(ServerIp, port);
        try {
            ds = new DatagramSocket(inetSocketAddress);
            Log.e(TAG, "UDP-Server已啟動");
        } catch (SocketException e) {
            Log.e(TAG, "啟動失敗，原因: " + e.getMessage());
            e.printStackTrace();
        }
        //預備一組byteArray來放入回傳得到的值(PS.回傳為格式為byte[]，本範例將值轉為字串了)
        byte[] msgRcv = new byte[1024];
        DatagramPacket dpRcv = new DatagramPacket(msgRcv, msgRcv.length);
        //建立while迴圈持續監聽來訪的數值
        while (isOpen) {
            Log.e(TAG, "UDP-Server監聽資訊中..");
            try {
                //執行緒將會在此打住等待有值出現
                ds.receive(dpRcv);
                String string = new String(dpRcv.getData(), dpRcv.getOffset(), dpRcv.getLength());
                Log.d(TAG, "UDP-Server收到資料： " + string);

                /**以Intent的方式建立廣播，將得到的值傳至主要Activity*/
                Intent intent = new Intent();
                intent.setAction(RECEIVE_ACTION);
                intent.putExtra(RECEIVE_STRING,string);
                intent.putExtra(RECEIVE_BYTES, dpRcv.getData());
                context.sendBroadcast(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
