package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener, ServiceConnection {
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(this);
        bindService(new Intent(this, MainProcessService.class), this, Context.BIND_AUTO_CREATE);
        bindService(new Intent(this, NewProcessService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button1:
//    android:text="start AsyncTask"/>
                new MyAsyncTask(button1).execute();
                break;
            case R.id.button2:
//    android:text="start Thread"/>
                new MyThread().start();
                break;
            case R.id.button3:
//    android:text="start Service"/>
                if (iMainProcessFunction == null) {
                    Toast.makeText(this, "iMainProcessFunction is null", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Toast.makeText(this, "result form main process:" + iMainProcessFunction.add(1, 2), Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.button4:
                if (iNewProcessFunction == null) {
                    Toast.makeText(this, "iNewProcessFunction is null", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Toast.makeText(this, "result form new process:" + iNewProcessFunction.add(1, 2), Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    IMainProcessFunction iMainProcessFunction = null;
    INewProcessFunction iNewProcessFunction = null;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            String interfaceDescriptor = service.getInterfaceDescriptor();
            if (IMainProcessFunction.class.getName().equals(interfaceDescriptor)) {
                iMainProcessFunction = IMainProcessFunction.Stub.asInterface(service);
            } else if (INewProcessFunction.class.getName().equals(interfaceDescriptor)) {
                iNewProcessFunction = INewProcessFunction.Stub.asInterface(service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
