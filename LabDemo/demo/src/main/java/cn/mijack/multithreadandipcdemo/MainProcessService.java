package cn.mijack.multithreadandipcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by Mr.Yuan on 2017/3/29.
 */

public class MainProcessService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new MainProcessBinder();
    }

    public static class MainProcessBinder extends IMainProcessFunction.Stub {

        @Override
        public int add(int num1, int num2) throws RemoteException {
            return num1 + num2;
        }
    }
}
