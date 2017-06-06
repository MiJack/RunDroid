package cn.mijack.multithreadandipcdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by Mr.Yuan on 2017/3/29.
 */

public class NewProcessService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new NewProcessBinder();
    }

    public static class NewProcessBinder extends INewProcessFunction.Stub {

        @Override
        public int add(int num1, int num2) throws RemoteException {
            return num1 + num2;
        }
    }
}
