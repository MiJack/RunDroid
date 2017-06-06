package com.mijack;

import android.os.Process;
import android.util.Log;


/**
* Created by Mr.Yuan on 2017/3/31.
*/
public class Xlog {
    public static final String TAG = "Xlog";
    public static final String LOG_TYPE_EXECUTE = "execute";
    public static final String LOG_TYPE_ENTER = "enter";
    public static final String LOG_TYPE_EXIT = "exit";
    public static final String INSTANCE_METHOD_TYPE = "instance_method_type";
    public static final String STATIC_METHOD_TYPE = "static_method_type";

    //static ObjectMapper objectMapper = new ObjectMapper();
    public static void logMethodExecute(String methodSign, Object instance, Object... args) {
        logMethodExecuteInfo(INSTANCE_METHOD_TYPE, methodSign, instance, args);
    }

    public static void logStaticMethodExecute(String methodSign, Object... args) {
        logMethodExecuteInfo(STATIC_METHOD_TYPE, methodSign, null, args);
    }

    //Xlog.logMethodEnter(System.identityHashCode(param),method2String(param.method), param.thisObject, param.args);
    public static void logMethodEnter(String methodSign, Object instance, Object... args) {
        logMethodEnter(-1, methodSign, instance, args);
    }

    public static void logMethodEnter(int hookId, String methodSign, Object instance, Object... args) {
        logMethodEnterInfo(hookId, INSTANCE_METHOD_TYPE, methodSign, instance, args);
    }

    //Xlog.logStaticMethodEnter(System.identityHashCode(param),method2String(param.method), param.args);
    public static void logStaticMethodEnter(String methodSign, Object... args) {
        logStaticMethodEnter(-1, methodSign, args);
    }

    public static void logStaticMethodEnter(int hookId, String methodSign, Object... args) {
        logMethodEnterInfo(hookId, STATIC_METHOD_TYPE, methodSign, null, args);
    }
//Xlog.logStaticMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.getThrowable());
public static void logStaticMethodExitWithThrowable( String methodSign, Throwable throwable) {logStaticMethodExitWithThrowable(-1,methodSign, throwable);}
public static void logStaticMethodExitWithThrowable(int hookId, String methodSign, Throwable throwable) {
        logMethodExitInfo(hookId,STATIC_METHOD_TYPE,methodSign,-1,null,throwable);
}
//Xlog.logStaticMethodExit(System.identityHashCode(param),method2String(param.method));
public static void logStaticMethodExit(String methodSign) {logStaticMethodExit(-1,methodSign);}
public static void logStaticMethodExit(int hookId, String methodSign) {
    logMethodExitInfo(hookId,STATIC_METHOD_TYPE,methodSign,-1,null,null);
}
public static void logStaticMethodExit(String methodSign, int index) {
    logMethodExitInfo(-1,STATIC_METHOD_TYPE,methodSign,index,null,null);
}
//Xlog.logMethodExitWithThrowable(System.identityHashCode(param),method2String(param.method), param.thisObject, param.getThrowable());
public static void logMethodExitWithThrowable( String methodSign, Object instance, Throwable throwable) {logMethodExitWithThrowable(-1,methodSign, instance, throwable);}
public static void logMethodExitWithThrowable(int hookId, String methodSign, Object instance, Throwable throwable) {
    logMethodExitInfo(hookId,INSTANCE_METHOD_TYPE,methodSign,-1,instance,throwable);

}
//Xlog.logMethodExit(System.identityHashCode(param),method2String(param.method), param.thisObject);
public static void logMethodExit(String methodSign, Object instance) {logMethodExit(methodSign, instance,-1);}
public static void logMethodExit(String methodSign, Object instance, int index) {
    logMethodExitInfo(-1,INSTANCE_METHOD_TYPE,methodSign,index,instance,null);

}
public static void logMethodExit(int hookId, String methodSign, Object instance) {
    logMethodExitInfo(hookId,INSTANCE_METHOD_TYPE,methodSign,-1,instance,null);
}

    public static void logMethodExitInfo(int hookId,String methodType, String methodSign, int index, Object instance, Throwable throwable) {
        StringBuilder sb = new StringBuilder("{").append(String.format(KEY_TO_VALUE, "logType", LOG_TYPE_EXIT));
        sb.append(",").append(String.format(KEY_TO_VALUE, "time", System.currentTimeMillis()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "processName", XlogUtils.getProcessName()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "threadName", XlogUtils.getCurrentThreadInfo()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "pid", XlogUtils.getProcessId()));
        if (hookId > 0) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "hookId", hookId));
        }
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodType", methodType));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodSign", methodSign));
        if (index > 0) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "index", index));
        }
        if (INSTANCE_METHOD_TYPE.equals(methodType)) {
            sb.append(",").append(String.format(KEY_TO_VALUE2, "instance", XlogUtils.object2String(instance)));
        }
        if (throwable !=null){
            sb.append(",").append(String.format(KEY_TO_VALUE, "throwable", XlogUtils.object2String(throwable)));
        }
        sb.append("}");
        Log.d(TAG, sb.toString());
    }

    public static void logMethodExecuteInfo(String methodType, String methodSign, Object instance, Object... args) {
        StringBuilder sb = new StringBuilder("{").append(String.format(KEY_TO_VALUE, "logType", LOG_TYPE_EXECUTE));
        sb.append(",").append(String.format(KEY_TO_VALUE, "time", System.currentTimeMillis()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "processName", XlogUtils.getProcessName()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "threadName", XlogUtils.getCurrentThreadInfo()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "pid", XlogUtils.getProcessId()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodType", methodType));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodSign", methodSign));
//判断是否是方法开始
        if (INSTANCE_METHOD_TYPE.equals(methodType)) {
            sb.append(",").append(String.format(KEY_TO_VALUE2, "instance", XlogUtils.object2String(instance)));
        }
        sb.append(",").append(XlogUtils.paramsToString(args));
        sb.append("}");
        Log.d(TAG, sb.toString());
    }

    public static void logMethodEnterInfo(int hookId, String methodType, String methodSign, Object instance, Object... args) {
        StringBuilder sb = new StringBuilder("{").append(String.format(KEY_TO_VALUE, "logType", LOG_TYPE_ENTER));
        sb.append(",").append(String.format(KEY_TO_VALUE, "time", System.currentTimeMillis()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "processName", XlogUtils.getProcessName()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "threadName", XlogUtils.getCurrentThreadInfo()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "pid", XlogUtils.getProcessId()));
        if (hookId > 0) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "hookId", hookId));
        }
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodType", methodType));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodSign", methodSign));
//判断是否是方法开始
        if (INSTANCE_METHOD_TYPE.equals(methodType)) {
            sb.append(",").append(String.format(KEY_TO_VALUE2, "instance", XlogUtils.object2String(instance)));
        }
        sb.append(",").append(XlogUtils.paramsToString(args));
        sb.append("}");
        Log.d(TAG, sb.toString());
    }

    public static final String KEY_TO_VALUE = "\"%s\":\"%s\"";
    public static final String KEY_TO_VALUE2 = "\"%s\":%s";

    public static void logStaticMethodExitWithResult(int hookId, String methodSign, Object result) {
        StringBuilder sb = new StringBuilder("{").append(String.format(KEY_TO_VALUE, "logType", LOG_TYPE_EXIT));
        sb.append(",").append(String.format(KEY_TO_VALUE, "time", System.currentTimeMillis()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "processName", XlogUtils.getProcessName()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "threadName", XlogUtils.getCurrentThreadInfo()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "pid", XlogUtils.getProcessId()));
        if (hookId > 0) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "hookId", hookId));
        }
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodType", STATIC_METHOD_TYPE));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodSign", methodSign));
        sb.append(",").append(String.format(KEY_TO_VALUE2, "result", XlogUtils.object2String(result)));
        sb.append("}");
        Log.d(TAG, sb.toString());
    }

    public static void logMethodExitWithResult(int hookId, String methodSign, Object instance, Object result) {
        StringBuilder sb = new StringBuilder("{").append(String.format(KEY_TO_VALUE, "logType", LOG_TYPE_EXIT));
        sb.append(",").append(String.format(KEY_TO_VALUE, "time", System.currentTimeMillis()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "processName", XlogUtils.getProcessName()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "threadName", XlogUtils.getCurrentThreadInfo()));
        sb.append(",").append(String.format(KEY_TO_VALUE, "pid", XlogUtils.getProcessId()));
        if (hookId > 0) {
            sb.append(",").append(String.format(KEY_TO_VALUE, "hookId", hookId));
        }
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodType", INSTANCE_METHOD_TYPE));
        sb.append(",").append(String.format(KEY_TO_VALUE, "methodSign", methodSign));
        sb.append(",").append(String.format(KEY_TO_VALUE2, "instance", XlogUtils.object2String(instance)));
        sb.append(",").append(String.format(KEY_TO_VALUE2, "result", XlogUtils.object2String(result)));
        sb.append("}");
        Log.d(TAG, sb.toString());
    }
}
