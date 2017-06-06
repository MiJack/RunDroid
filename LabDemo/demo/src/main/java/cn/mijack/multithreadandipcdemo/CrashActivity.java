package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Mr.Yuan
 * @date 2017/5/15
 */
public class CrashActivity extends Activity implements View.OnClickListener {
    Button button;
    TextView textView;
    Boolean status = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        System.out.println("status:" + status.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                changeStatus(null);
                int count = 3;
                for (int i = 0; i < count; i++) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("time:" + (finalI * 1000) + "ms");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("You can click again without crash");
                    }
                });
                changeStatus(true);
            }
        }).start();
    }

    private void changeStatus(Boolean aBoolean) {
        status = aBoolean;
    }
}
