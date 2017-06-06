package cn.mijack.multithreadandipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ThreadActivity extends Activity implements View.OnClickListener {

    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastNoToast();
            }

            private void toastNoToast() {
                Toast.makeText(ThreadActivity.this, "No Task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startNewThread(View v) {
        new MyThread().start();
    }

    public void invokeMultiMethods(View view) {
        a();
        c();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button3) {
            changeButtonListener();
        } else if (id == R.id.button4) {
            // run AsyncTask
            log();
        }
    }

    private void changeButtonListener() {
        Button button = (Button) findViewById(R.id.button5);
        button.setText("Toast");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThreadActivity.this, "this is a toast", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void log() {
        Log.d("ThreadActivity", "This is a log ");
    }

    private void runAsyncTask(Button v) {
        myAsyncTask = new MyAsyncTask(v);
        myAsyncTask.execute();
    }

    private void c() {
        System.out.println("c");
    }

    private void a() {
        b();
    }

    private void b() {
        System.out.println("b");
    }

    public void runOnUiThreadLater(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ThreadActivity.this, "toast", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    public static class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(i);
            }
        }
    }

}
