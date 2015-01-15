package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.view.View.OnClickListener;


public class ShellActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        Button mButton1 = (Button) findViewById(R.id.btn1);
        mButton1.setOnClickListener(this);
        Button mButton2 = (Button) findViewById(R.id.btn2);
        mButton2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1 :
                startService(CamService.getIntent());
                break;
            case R.id.btn2 :
                stopService(CamService.getIntent());
                break;
        }
    }
}
