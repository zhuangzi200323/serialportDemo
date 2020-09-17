package com.jack.serialprotdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jack.serialprot.SerialPortCallback;
import com.jack.utils.ByteStringHexUtils;

import java.nio.ByteBuffer;

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
                    //SimpleSerialPortHelper.getInstance().open();
                    SerialPortMgr.getInstance().getTest1SerialPortHelper().open();
                }catch (Exception err){
                    Log.e(TAG, "open serial port err", err);
                }
                break;
            case R.id.close_btn:
                //SimpleSerialPortHelper.getInstance().close();
                SerialPortMgr.getInstance().getTest1SerialPortHelper().close();
                break;
            case R.id.send_data_btn:
                //SimpleSerialPortHelper.getInstance().sendTxt("this is a test.");
                ByteBuffer byteBuffer = ByteBuffer.wrap("this is a test.".getBytes());
                SerialPortMgr.getInstance().getTest1SerialPortHelper().post(byteBuffer, 1, 3000, new SerialPortCallback<byte[]>() {
                    @Override
                    public void success(byte[] result) {
                        Log.e(TAG, "##### " + ByteStringHexUtils.ByteToString(result));
                    }

                    @Override
                    public void failed(int errCode, String errInfo, String result) {
                        Log.e(TAG, "##### errCode = " + errCode + ", errInfo = " + errInfo + ", result = " + result);
                    }
                });
                break;
            default:
                break;
        }
    }
}
