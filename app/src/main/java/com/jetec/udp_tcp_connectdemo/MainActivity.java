package com.jetec.udp_tcp_connectdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.jetec.udp_tcp_connectdemo.TCP.TCPActivity;
import com.jetec.udp_tcp_connectdemo.UDP.UDPActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_ToTCP).setOnClickListener(v->{
            startActivity(new Intent(this, TCPActivity.class));
        });

        findViewById(R.id.button_ToUDP).setOnClickListener(v -> {
            startActivity(new Intent(this, UDPActivity.class));
        });
    }
}