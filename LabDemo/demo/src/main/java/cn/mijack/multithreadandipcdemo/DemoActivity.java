package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Mr.Yuan
 * @date 2017/5/26
 */
public class DemoActivity extends Activity implements View.OnClickListener {
    protected EditText editText;
    protected Button btn0;
    protected Button btn1;
    protected Button btn2;
    protected Integer number;
    private static final int MSG_CHANGE_TEXT = 1;
    public static final String TAG = "DemoActivity";


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CHANGE_TEXT) {
                TextView textView = (TextView) findViewById(R.id.tv);
                textView.setText(msg.obj.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        editText = (EditText) findViewById(R.id.editText);
        btn0 = (Button) findViewById(R.id.btn0);
        btn0.setOnClickListener(this);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn0) {
            toastClick();
        } else if (v.getId() == R.id.btn1) {
            number = getNumber();
            if (number == 0) {
                number = 1;
            }
            Thread t = createTread(1);
            t.start();
        } else if (v.getId() == R.id.btn2) {
            Thread t = createTread(2);
            t.start();
        }
    }

    private Thread createTread(int i) {
        Thread thread = null;
        if (i == 1) {
            thread = new TaskThread();
        } else {
            thread = new SleepThread();
        }
        return thread;
    }

    private int getNumber() {
        return 0;
    }

    private void toastClick() {
        Toast.makeText(this, "click a button", Toast.LENGTH_SHORT).show();
    }

    public class TaskThread extends Thread {
        @Override
        public void run() {
            if (number != null) {
                saveData(number);
            }
        }

        private void saveData(Integer number) {
            Log.d(TAG, "saveData: " + number);
        }

    }

    private class SleepThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message = Message.obtain();
            message.what = MSG_CHANGE_TEXT;
            message.obj = "Set up time at time" + System.currentTimeMillis();
            handler.sendMessage(message);
        }
    }
}
