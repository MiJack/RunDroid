package com.mijack.xposed;

/**
 * Created by Mr.Yuan on 2017/4/5.
 */

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by Mr.Yuan on 2017/4/1.
 */

public class O2S {
    /**
     * 将object转化成json
     *
     * @param o
     * @return
     */
    public static String object2String(Object o) {
        if (o == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("class:").append(o.getClass().getName())
                .append(",hashcode:").append(System.identityHashCode(o));
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            sb.append(",message:").append(throwable.getMessage());
        } else if (o instanceof ComponentName) {
            ComponentName componentName = (ComponentName) o;
            sb.append(",packageName:").append(componentName.getPackageName())
                    .append(",shortName:").append(componentName.getShortClassName());
        } else if (o instanceof IBinder) {
            IBinder iBinder = (IBinder) o;
            String descriptor;
            try {
                descriptor = iBinder.getInterfaceDescriptor();
            } catch (RemoteException e) {
                e.printStackTrace();
                descriptor = "null";
            }
            sb.append(",descriptor:").append(descriptor);
        }
        sb.append("}");
        return sb.toString();
    }
}
