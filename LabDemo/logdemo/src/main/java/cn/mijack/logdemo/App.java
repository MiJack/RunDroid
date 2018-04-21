package cn.mijack.logdemo;

import android.app.Application;

/**
 * @author Mi&Jack
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogInit.init(this);
    }
}

