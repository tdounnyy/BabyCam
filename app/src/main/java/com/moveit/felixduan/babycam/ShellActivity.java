package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class ShellActivity extends Activity implements View.OnClickListener {

    private Button mButton1;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        mButton1 = (Button) findViewById(R.id.btn1);
        mButton1.setOnClickListener(this);
        mButton2 = (Button) findViewById(R.id.btn2);
        mButton2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1 :
                startService(new Intent("felix.duan.CamService"));
                break;
            case R.id.btn2 :
                stopService(new Intent("felix.duan.CamService"));
                break;
        }
    }
}
