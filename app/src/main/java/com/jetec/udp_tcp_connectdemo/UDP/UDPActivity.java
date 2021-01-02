package com.jetec.udp_tcp_connectdemo.UDP;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.jetec.udp_tcp_connectdemo.R;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.jetec.udp_tcp_connectdemo.CommendFun.getLocalIP;

public class UDPActivity extends AppCompatActivity {

    EditText edRemoteIp, edLocalPort, edReceiveMessage, edInputBox, edRemotePort;
    MyBroadcast myBroadcast = new MyBroadcast();
    StringBuffer stringBuffer = new StringBuffer();
    ExecutorService exec = Executors.newCachedThreadPool();
    UDP udpServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_u_d_p);
        //設置基礎UI
        setBaseUI();
        //設置監UDP功能
        setReceiveSwitch();
        //設置發送資料功能
        setSendFunction();
        //註冊廣播器，使回傳能夠從其他類別內傳回此Activity
        IntentFilter intentFilter = new IntentFilter(UDP.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
    }

    private void setBaseUI() {
        TextView tvLocalIp = findViewById(R.id.textView_IP);
        //注意：此處有調用CommendFun.java的內容以取得本機IP
        tvLocalIp.setText("本機IP: " + getLocalIP(this));
        //清除所有儲存過的資訊
        Button btClear = findViewById(R.id.button_clear);
        btClear.setOnClickListener(v -> {
            stringBuffer.delete(0,stringBuffer.length());
            edReceiveMessage.setText(stringBuffer);
        });
        edRemoteIp = findViewById(R.id.editText_RemoteIp);
        edRemotePort = findViewById(R.id.editText_RemotePort);
        edLocalPort = findViewById(R.id.editText_Port);
        edReceiveMessage = findViewById(R.id.editText_ReceiveMessage);
        edInputBox = findViewById(R.id.editText_Input);
    }

    private void setReceiveSwitch() {
        ToggleButton btSwitch = findViewById(R.id.toggleButton_ReceiveSwitch);
        //初始化UDP伺服器
        //注意：此處有調用CommendFun.java的內容以取得本機IP
        udpServer = new UDP(getLocalIP(this),this);
        btSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                /**開啟UDP伺服器監聽*/
                int port = Integer.parseInt(edLocalPort.getText().toString());
                udpServer.setPort(port);
                udpServer.changeServerStatus(true);
                exec.execute(udpServer);
                edLocalPort.setEnabled(false);
            } else {
                /**關閉UDP伺服器監聽*/
                udpServer.changeServerStatus(false);
                edLocalPort.setEnabled(true);
            }
        });
    }

    private void setSendFunction() {
        Button btSend = findViewById(R.id.button_Send);
        /**發送UDP訊息至指定的IP*/
        btSend.setOnClickListener(v -> {
            String msg = edInputBox.getText().toString();
            String remoteIp = edRemoteIp.getText().toString();
            int port = Integer.parseInt(edRemotePort.getText().toString());
            if (msg.length() == 0) return;
            stringBuffer.append("發送： ").append(msg).append("\n");
            edReceiveMessage.setText(stringBuffer);
            //調用UDP.java中的方法，送出資料
            //本範例用lambda表達式，原貌在下面註解
            exec.execute(()->{
                try {
                udpServer.send(msg, remoteIp, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
//           exec.execute(new Runnable() {
//               @Override
//               public void run() {
//                try {
//                    udpServer.send(msg, remoteIp, port);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//              }
//           });
        });
    }

    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            assert mAction != null;
            switch (mAction) {
                /**接收來自UDP回傳之訊息*/
                case UDP.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(UDP.RECEIVE_STRING);
                    byte[] bytes = intent.getByteArrayExtra(UDP.RECEIVE_BYTES);
                    stringBuffer.append("收到： ").append(msg).append("\n");
                    edReceiveMessage.setText(stringBuffer);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消監聽廣播
        unregisterReceiver(myBroadcast);
    }

}