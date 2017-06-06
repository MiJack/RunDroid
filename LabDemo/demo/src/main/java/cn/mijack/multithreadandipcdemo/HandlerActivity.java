package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class HandlerActivity extends Activity {
    private static final int MSG_CHANGE_TEXT = 1;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_CHANGE_TEXT:
                    ((TextView)findViewById(R.id.tv)).setText("Time is up!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler);
        findViewById(R.id.btnSendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(MSG_CHANGE_TEXT);
                    }
                }).start();
            }
        });
    }
}
