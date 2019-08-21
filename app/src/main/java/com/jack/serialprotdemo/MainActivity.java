package com.jack.serialprotdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open_btn).setOnClickListener(this);
        findViewById(R.id.close_btn).setOnClickListener(this);
        findViewById(R.id.send_data_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.open_btn:
                try {
                    SerialPortHelper.getInstance().open();
                }catch (Exception err){
                    Log.e(TAG, "open serial port err", err);
                }
                break;
            case R.id.close_btn:
                SerialPortHelper.getInstance().close();
                break;
            case R.id.send_data_btn:
                SerialPortHelper.getInstance().sendTxt("this is a test.");
                break;
        }
    }
}
