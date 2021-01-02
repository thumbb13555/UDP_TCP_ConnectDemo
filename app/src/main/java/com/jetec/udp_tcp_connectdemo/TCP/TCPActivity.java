package com.jetec.udp_tcp_connectdemo.TCP;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.jetec.udp_tcp_connectdemo.R;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.jetec.udp_tcp_connectdemo.CommendFun.getLocalIP;


public class TCPActivity extends AppCompatActivity {

    EditText edRemoteIp, edLocalPort, edReceiveMessage, edInputBox, edRemotePort;
    ToggleButton btClientConnect, btOpenServer, btServer;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swFunction;
    ExecutorService exec = Executors.newCachedThreadPool();
    MyBroadcast myBroadcast = new MyBroadcast();
    StringBuffer stringBuffer = new StringBuffer();
    TCPServer tcpServer;
    TCPClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t_c_p);
        //設置基本UI
        setBaseUI();
        //設置TCP伺服器功能
        setServerSwitch();
        //設置TCP客戶端功能
        setClientSwitch();
        //設置發送資料功能(含伺服器/客戶端)
        setSendFunction();
        //註冊廣播，接收來自對方設備回傳的資料
        IntentFilter intentFilter = new IntentFilter(TCPServer.RECEIVE_ACTION);
        registerReceiver(myBroadcast, intentFilter);
    }

    private void setBaseUI() {
        TextView tvLocalIp = findViewById(R.id.textView_IP);
        tvLocalIp.setText("本機IP: " + getLocalIP(this));
        Button btClear = findViewById(R.id.button_clear);
        btClear.setOnClickListener(v -> {
            stringBuffer.delete(0, stringBuffer.length());
            edReceiveMessage.setText(stringBuffer);
        });
        btOpenServer = findViewById(R.id.toggleButton_Server);
        btClientConnect = findViewById(R.id.toggleButton_ClientConnect);
        edRemoteIp = findViewById(R.id.editText_RemoteIp);
        edRemotePort = findViewById(R.id.editText_RemotePort);
        edLocalPort = findViewById(R.id.editText_Port);
        edReceiveMessage = findViewById(R.id.editText_ReceiveMessage);
        edInputBox = findViewById(R.id.editText_Input);
        swFunction = findViewById(R.id.switch_ModeChange);
        swFunction.setChecked(false);
        //設置模式切換之Switch
        swFunction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btOpenServer.setEnabled(!isChecked);
            btClientConnect.setEnabled(isChecked);
            if (!isChecked) {
                if (tcpClient != null && tcpClient.getStatus()) {
                    tcpClient.closeClient();
                    btClientConnect.setChecked(false);
                }
                swFunction.setText("Server");
            } else {
                if (tcpServer != null && tcpServer.getStatus()) {
                    tcpServer.closeServer();
                    btServer.setChecked(false);
                }
                swFunction.setText("Client");
            }
        });
    }
    /**設置TCP伺服器功能*/
    private void setServerSwitch() {
        btServer = findViewById(R.id.toggleButton_Server);
        ExecutorService exec = Executors.newCachedThreadPool();
        btServer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                int port = Integer.parseInt(edLocalPort.getText().toString());
                tcpServer = new TCPServer(port, this);
                exec.execute(tcpServer);
                edLocalPort.setEnabled(false);
            } else {
                tcpServer.closeServer();
                edLocalPort.setEnabled(true);
            }
        });

    }
    /**設置TCP客戶端功能*/
    private void setClientSwitch() {
        String remoteIp = edRemoteIp.getText().toString();
        int remotePort = Integer.parseInt(edRemotePort.getText().toString());
        btClientConnect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tcpClient = new TCPClient(remoteIp, remotePort, this);
                exec.execute(tcpClient);
                edRemoteIp.setEnabled(false);
                edRemotePort.setEnabled(false);

            } else {
                tcpClient.closeClient();
                edRemoteIp.setEnabled(true);
                edRemotePort.setEnabled(true);
            }
        });


    }
    /**設置發送資料功能(含伺服器/客戶端)*/
    private void setSendFunction() {
        Button btSend = findViewById(R.id.button_Send);
        btSend.setOnClickListener(v -> {
            String text = edInputBox.getText().toString();
            if (swFunction.isChecked()) {
                //切換開關在Client模式時
                if (tcpClient == null)return;
                if (text.length() == 0 || !tcpClient.getStatus()) return;
                exec.execute(() -> tcpClient.send(text));
            } else {
                //切換開關在Server模式時
                if (tcpServer == null)return;
                if (text.length() == 0 || !tcpServer.getStatus()) return;
                //此處Lambda表達式相等於下方註解部分
                if (tcpServer.SST.size() == 0) return;
                exec.execute(() -> tcpServer.SST.get(0).sendData(text));
//           exec.execute(new Runnable() {
//               @Override
//               public void run() {
//                   tcpServer.SST.get(0).sendData(text);
//               }
//           });
            }
        });
    }
    private class MyBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            assert mAction != null;
            /**接收來自UDP回傳之訊息*/
            switch (mAction) {
                case TCPServer.RECEIVE_ACTION:
                    String msg = intent.getStringExtra(TCPServer.RECEIVE_STRING);
                    byte[] bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES);
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