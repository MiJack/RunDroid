package cn.mijack.multithreadandipcdemo;

import android.util.Log;

/**
 * Created by Mr.Yuan on 2017/3/29.
 */

public class MyThread extends Thread {
    private static final String TAG = MyThread.class.getName();

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            Log.d(TAG, String.format("run: %d", i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
